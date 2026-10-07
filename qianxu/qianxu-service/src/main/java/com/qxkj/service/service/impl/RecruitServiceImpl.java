package com.qxkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qxkj.common.constants.SmsConstants;
import com.qxkj.common.model.recruit.RecruitApply;
import com.qxkj.common.model.recruit.RecruitJob;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.RecruitRequests;
import com.qxkj.common.utils.RedisUtil;
import com.qxkj.service.dao.RecruitApplyDao;
import com.qxkj.service.dao.RecruitJobDao;
import com.qxkj.service.service.RecruitService;
import com.qxkj.service.service.SmsService;
import com.qxkj.service.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 招聘系统实现（后台发布职位 + 会员端投递 + 后台跟进）
 */
@Slf4j
@Service
public class RecruitServiceImpl implements RecruitService {

    /** 招聘设置的配置键 */
    private static final String[] SETTING_KEYS = {
            "rec_home_mode", "rec_sms_verify", "rec_company_name",
            "rec_banner_title", "rec_banner_desc",
            "rec_contact_name", "rec_contact_phone", "rec_contact_wechat", "rec_tip"
    };

    private static final Gson GSON = new Gson();

    @Resource
    private RecruitJobDao recruitJobDao;

    @Resource
    private RecruitApplyDao recruitApplyDao;

    @Resource
    private RedisUtil redisUtil;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private SmsService smsService;

    // ==================== 后台：职位管理 ====================

    @Override
    public CommonPage<RecruitJob> adminJobList(String keywords, String category, Integer status, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        LambdaQueryWrapper<RecruitJob> lqw = new LambdaQueryWrapper<>();
        lqw.eq(RecruitJob::getIsDel, 0);
        if (status != null) {
            lqw.eq(RecruitJob::getStatus, status);
        }
        if (StringUtils.isNotBlank(category)) {
            lqw.eq(RecruitJob::getCategory, category);
        }
        if (StringUtils.isNotBlank(keywords)) {
            lqw.and(w -> w.like(RecruitJob::getTitle, keywords)
                    .or().like(RecruitJob::getDept, keywords)
                    .or().like(RecruitJob::getCity, keywords));
        }
        lqw.orderByDesc(RecruitJob::getSort).orderByDesc(RecruitJob::getId);
        List<RecruitJob> list = recruitJobDao.selectList(lqw);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public RecruitJob adminJobInfo(Integer id) {
        RecruitJob job = recruitJobDao.selectById(id);
        if (job == null || job.getIsDel() > 0) {
            throw new RuntimeException("职位不存在");
        }
        return job;
    }

    @Override
    public Integer saveJob(RecruitRequests.JobSaveRequest request) {
        if (request.getId() != null && request.getId() > 0) {
            RecruitJob exist = recruitJobDao.selectById(request.getId());
            if (exist == null || exist.getIsDel() > 0) {
                throw new RuntimeException("职位不存在");
            }
            fillJob(exist, request);
            recruitJobDao.updateById(exist);
            return exist.getId();
        }
        RecruitJob job = new RecruitJob();
        fillJob(job, request);
        job.setViewCount(0);
        job.setApplyCount(0);
        job.setIsDel(0);
        if (StringUtils.isBlank(job.getOnlineDate())) {
            job.setOnlineDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        }
        if (job.getStatus() == null) {
            job.setStatus(RecruitJob.STATUS_ON);
        }
        recruitJobDao.insert(job);
        return job.getId();
    }

    private void fillJob(RecruitJob job, RecruitRequests.JobSaveRequest request) {
        job.setTitle(blankToEmpty(request.getTitle()));
        job.setCategory(blankToEmpty(request.getCategory()));
        job.setDept(blankToEmpty(request.getDept()));
        job.setCity(blankToEmpty(request.getCity()));
        job.setAddress(blankToEmpty(request.getAddress()));
        job.setJobType(StringUtils.isBlank(request.getJobType()) ? "全职" : request.getJobType());
        job.setSalary(blankToEmpty(request.getSalary()));
        job.setSalaryMin(request.getSalaryMin() == null ? 0 : request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax() == null ? 0 : request.getSalaryMax());
        job.setRecruitNum(request.getRecruitNum() == null ? 1 : request.getRecruitNum());
        job.setExperience(StringUtils.isBlank(request.getExperience()) ? "不限" : request.getExperience());
        job.setEducation(StringUtils.isBlank(request.getEducation()) ? "不限" : request.getEducation());
        job.setTags(blankToEmpty(request.getTags()));
        job.setDescription(blankToEmpty(request.getDescription()));
        job.setRequirement(blankToEmpty(request.getRequirement()));
        job.setHrName(blankToEmpty(request.getHrName()));
        job.setHrPhone(blankToEmpty(request.getHrPhone()));
        job.setIsHot(request.getIsHot() == null ? 0 : request.getIsHot());
        job.setSort(request.getSort() == null ? 0 : request.getSort());
        job.setOnlineDate(blankToEmpty(request.getOnlineDate()));
        if (request.getStatus() != null) {
            job.setStatus(request.getStatus());
        } else if (job.getStatus() == null) {
            job.setStatus(RecruitJob.STATUS_ON);
        }
    }

    @Override
    public Boolean updateJobStatus(Integer id, Integer status) {
        RecruitJob job = adminJobInfo(id);
        job.setStatus(status == null ? RecruitJob.STATUS_OFF : status);
        return recruitJobDao.updateById(job) > 0;
    }

    @Override
    public Boolean deleteJob(Integer id) {
        RecruitJob job = adminJobInfo(id);
        job.setIsDel(1);
        return recruitJobDao.updateById(job) > 0;
    }

    @Override
    public HashMap<String, Object> jobStats() {
        HashMap<String, Object> map = new HashMap<>();
        LambdaQueryWrapper<RecruitJob> onLqw = new LambdaQueryWrapper<>();
        onLqw.eq(RecruitJob::getIsDel, 0).eq(RecruitJob::getStatus, RecruitJob.STATUS_ON);
        map.put("jobOn", recruitJobDao.selectCount(onLqw));

        LambdaQueryWrapper<RecruitJob> offLqw = new LambdaQueryWrapper<>();
        offLqw.eq(RecruitJob::getIsDel, 0).eq(RecruitJob::getStatus, RecruitJob.STATUS_OFF);
        map.put("jobOff", recruitJobDao.selectCount(offLqw));

        LambdaQueryWrapper<RecruitApply> applyLqw = new LambdaQueryWrapper<>();
        applyLqw.eq(RecruitApply::getIsDel, 0);
        map.put("applyTotal", recruitApplyDao.selectCount(applyLqw));

        LambdaQueryWrapper<RecruitApply> waitLqw = new LambdaQueryWrapper<>();
        waitLqw.eq(RecruitApply::getIsDel, 0).eq(RecruitApply::getStatus, RecruitApply.STATUS_WAIT);
        map.put("applyWait", recruitApplyDao.selectCount(waitLqw));

        LambdaQueryWrapper<RecruitApply> monthLqw = new LambdaQueryWrapper<>();
        monthLqw.eq(RecruitApply::getIsDel, 0);
        monthLqw.apply("date_format(create_time,'%Y-%m') = date_format(now(),'%Y-%m')");
        map.put("applyMonth", recruitApplyDao.selectCount(monthLqw));
        return map;
    }

    // ==================== 后台：应聘管理 ====================

    @Override
    public CommonPage<RecruitApply> adminApplyList(String keywords, Integer jobId, Integer status, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        LambdaQueryWrapper<RecruitApply> lqw = new LambdaQueryWrapper<>();
        lqw.eq(RecruitApply::getIsDel, 0);
        if (status != null) {
            lqw.eq(RecruitApply::getStatus, status);
        }
        if (jobId != null && jobId > 0) {
            lqw.eq(RecruitApply::getJobId, jobId);
        }
        if (StringUtils.isNotBlank(keywords)) {
            lqw.and(w -> w.like(RecruitApply::getName, keywords)
                    .or().like(RecruitApply::getPhone, keywords)
                    .or().like(RecruitApply::getJobTitle, keywords));
        }
        lqw.orderByDesc(RecruitApply::getId);
        List<RecruitApply> list = recruitApplyDao.selectList(lqw);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public RecruitApply adminApplyInfo(Integer id) {
        RecruitApply apply = recruitApplyDao.selectById(id);
        if (apply == null || apply.getIsDel() > 0) {
            throw new RuntimeException("应聘记录不存在");
        }
        return apply;
    }

    @Override
    public Boolean handleApply(RecruitRequests.ApplyHandleRequest request) {
        RecruitApply apply = adminApplyInfo(request.getId());
        if (request.getStatus() != null) {
            apply.setStatus(request.getStatus());
        }
        if (request.getAdminRemark() != null) {
            apply.setAdminRemark(request.getAdminRemark());
        }
        if (request.getInterviewDate() != null) {
            apply.setInterviewDate(request.getInterviewDate());
        }
        apply.setHandler(blankToEmpty(request.getHandler()));
        apply.setLog(appendLog(apply.getLog(), statusText(apply.getStatus()),
                blankToEmpty(request.getHandler()),
                StringUtils.isBlank(request.getAdminRemark()) ? statusText(apply.getStatus()) : request.getAdminRemark()));
        return recruitApplyDao.updateById(apply) > 0;
    }

    @Override
    public Boolean deleteApply(Integer id) {
        RecruitApply apply = adminApplyInfo(id);
        apply.setIsDel(1);
        boolean ok = recruitApplyDao.updateById(apply) > 0;
        if (ok && apply.getJobId() != null && apply.getJobId() > 0) {
            RecruitJob job = recruitJobDao.selectById(apply.getJobId());
            if (job != null) {
                job.setApplyCount(Math.max(0, (job.getApplyCount() == null ? 0 : job.getApplyCount()) - 1));
                recruitJobDao.updateById(job);
            }
        }
        return ok;
    }

    @Override
    public HashMap<String, Object> applyStats() {
        HashMap<String, Object> map = new HashMap<>();
        int[] statuses = {RecruitApply.STATUS_WAIT, RecruitApply.STATUS_CONTACTED,
                RecruitApply.STATUS_INTERVIEW, RecruitApply.STATUS_HIRED, RecruitApply.STATUS_REJECT};
        for (int s : statuses) {
            LambdaQueryWrapper<RecruitApply> lqw = new LambdaQueryWrapper<>();
            lqw.eq(RecruitApply::getIsDel, 0).eq(RecruitApply::getStatus, s);
            map.put("s" + s, recruitApplyDao.selectCount(lqw));
        }
        return map;
    }

    // ==================== 后台：招聘设置 ====================

    @Override
    public HashMap<String, Object> getSetting() {
        HashMap<String, Object> map = new HashMap<>();
        for (String key : SETTING_KEYS) {
            map.put(key, systemConfigService.getValueByKey(key));
        }
        return map;
    }

    @Override
    public Boolean saveSetting(HashMap<String, Object> settingMap) {
        for (String key : SETTING_KEYS) {
            if (settingMap.containsKey(key)) {
                Object v = settingMap.get(key);
                systemConfigService.updateOrSaveValueByName(key, v == null ? "" : String.valueOf(v));
            }
        }
        return true;
    }

    // ==================== 会员端 ====================

    @Override
    public HashMap<String, Object> getConfig() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("homeMode", "1".equals(systemConfigService.getValueByKey("rec_home_mode")));
        map.put("smsVerify", "1".equals(systemConfigService.getValueByKey("rec_sms_verify")));
        map.put("companyName", systemConfigService.getValueByKey("rec_company_name"));
        map.put("bannerTitle", systemConfigService.getValueByKey("rec_banner_title"));
        map.put("bannerDesc", systemConfigService.getValueByKey("rec_banner_desc"));
        map.put("contactName", systemConfigService.getValueByKey("rec_contact_name"));
        map.put("contactPhone", systemConfigService.getValueByKey("rec_contact_phone"));
        map.put("contactWechat", systemConfigService.getValueByKey("rec_contact_wechat"));
        map.put("tip", systemConfigService.getValueByKey("rec_tip"));
        return map;
    }

    @Override
    public CommonPage<RecruitJob> frontJobList(String keywords, String category, String city, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        LambdaQueryWrapper<RecruitJob> lqw = new LambdaQueryWrapper<>();
        lqw.eq(RecruitJob::getIsDel, 0);
        lqw.eq(RecruitJob::getStatus, RecruitJob.STATUS_ON);
        if (StringUtils.isNotBlank(category)) {
            lqw.eq(RecruitJob::getCategory, category);
        }
        if (StringUtils.isNotBlank(city)) {
            lqw.eq(RecruitJob::getCity, city);
        }
        if (StringUtils.isNotBlank(keywords)) {
            lqw.and(w -> w.like(RecruitJob::getTitle, keywords)
                    .or().like(RecruitJob::getDept, keywords)
                    .or().like(RecruitJob::getTags, keywords));
        }
        lqw.orderByDesc(RecruitJob::getSort).orderByDesc(RecruitJob::getId);
        List<RecruitJob> list = recruitJobDao.selectList(lqw);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public RecruitJob frontJobInfo(Integer id) {
        RecruitJob job = adminJobInfo(id);
        if (job.getStatus() == null || job.getStatus().intValue() != RecruitJob.STATUS_ON) {
            throw new RuntimeException("职位已关闭");
        }
        job.setViewCount((job.getViewCount() == null ? 0 : job.getViewCount()) + 1);
        recruitJobDao.updateById(job);
        return job;
    }

    @Override
    public List<HashMap<String, Object>> frontCategoryList() {
        LambdaQueryWrapper<RecruitJob> lqw = new LambdaQueryWrapper<>();
        lqw.eq(RecruitJob::getIsDel, 0);
        lqw.eq(RecruitJob::getStatus, RecruitJob.STATUS_ON);
        List<RecruitJob> jobs = recruitJobDao.selectList(lqw);
        List<HashMap<String, Object>> result = new ArrayList<>();
        HashMap<String, Integer> counter = new HashMap<>();
        for (RecruitJob job : jobs) {
            String cat = StringUtils.isBlank(job.getCategory()) ? "其他" : job.getCategory();
            counter.put(cat, counter.getOrDefault(cat, 0) + 1);
        }
        counter.forEach((k, v) -> {
            HashMap<String, Object> item = new HashMap<>();
            item.put("name", k);
            item.put("count", v);
            result.add(item);
        });
        result.sort((a, b) -> (Integer) b.get("count") - (Integer) a.get("count"));
        return result;
    }

    @Override
    public Integer submitApply(Integer uid, RecruitRequests.ApplySubmitRequest request) {
        RecruitApply apply = new RecruitApply();
        apply.setApplyNo(makeApplyNo());
        apply.setUid(uid == null ? 0 : uid);
        apply.setName(blankToEmpty(request.getName()));
        apply.setPhone(blankToEmpty(request.getPhone()));
        apply.setGender(blankToEmpty(request.getGender()));
        apply.setAge(blankToEmpty(request.getAge()));
        apply.setExperience(blankToEmpty(request.getExperience()));
        apply.setEducation(blankToEmpty(request.getEducation()));
        apply.setSchool(blankToEmpty(request.getSchool()));
        apply.setExpectCity(blankToEmpty(request.getExpectCity()));
        apply.setExpectSalary(blankToEmpty(request.getExpectSalary()));
        apply.setResume(blankToEmpty(request.getResume()));
        apply.setResumeName(blankToEmpty(request.getResumeName()));
        apply.setRemark(blankToEmpty(request.getRemark()));
        apply.setSource("h5");
        apply.setStatus(RecruitApply.STATUS_WAIT);
        apply.setAdminRemark("");
        apply.setHandler("");
        apply.setInterviewDate("");

        String jobTitle = "人才自荐";
        if (request.getJobId() != null && request.getJobId() > 0) {
            RecruitJob job = recruitJobDao.selectById(request.getJobId());
            if (job != null && job.getIsDel() == 0) {
                jobTitle = job.getTitle();
                job.setApplyCount((job.getApplyCount() == null ? 0 : job.getApplyCount()) + 1);
                recruitJobDao.updateById(job);
            }
        }
        apply.setJobId(request.getJobId() == null ? 0 : request.getJobId());
        apply.setJobTitle(jobTitle);
        apply.setLog(appendLog(null, "提交应聘", apply.getName(), "来源：会员端 H5"));
        apply.setIsDel(0);
        recruitApplyDao.insert(apply);
        log.info("招聘投递成功：{} {} 应聘 {}", apply.getApplyNo(), apply.getName(), jobTitle);
        return apply.getId();
    }

    @Override
    public CommonPage<RecruitApply> myApplyList(Integer uid, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        LambdaQueryWrapper<RecruitApply> lqw = new LambdaQueryWrapper<>();
        lqw.eq(RecruitApply::getIsDel, 0);
        lqw.eq(RecruitApply::getUid, uid);
        lqw.orderByDesc(RecruitApply::getId);
        List<RecruitApply> list = recruitApplyDao.selectList(lqw);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public RecruitApply myApplyInfo(Integer uid, Integer id) {
        RecruitApply apply = adminApplyInfo(id);
        if (apply.getUid() == null || !apply.getUid().equals(uid)) {
            throw new RuntimeException("无权查看该应聘记录");
        }
        return apply;
    }

    @Override
    public Boolean sendApplyCode(String phone) {
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号码错误");
        }
        return smsService.sendCommonCode(phone);
    }

    @Override
    public void checkApplyCode(String phone, String code) {
        if (StringUtils.isBlank(code)) {
            throw new RuntimeException("请输入短信验证码");
        }
        Object validateCode = redisUtil.get(SmsConstants.SMS_VALIDATE_PHONE + phone);
        if (validateCode == null) {
            throw new RuntimeException("验证码已过期");
        }
        if (!validateCode.toString().equals(code.trim())) {
            throw new RuntimeException("验证码错误");
        }
        redisUtil.delete(SmsConstants.SMS_VALIDATE_PHONE + phone);
    }

    // ==================== 私有工具 ====================

    private String makeApplyNo() {
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        int rand = (int) (Math.random() * 9000) + 1000;
        return "REC" + date + rand;
    }

    private String blankToEmpty(String s) {
        return s == null ? "" : s.trim();
    }

    private String statusText(Integer status) {
        if (status == null) {
            return "待联系";
        }
        switch (status) {
            case RecruitApply.STATUS_CONTACTED:
                return "已联系";
            case RecruitApply.STATUS_INTERVIEW:
                return "面试中";
            case RecruitApply.STATUS_HIRED:
                return "已录用";
            case RecruitApply.STATUS_REJECT:
                return "不合适";
            default:
                return "待联系";
        }
    }

    /**
     * 追加一条跟进日志（JSON 数组）
     */
    private String appendLog(String origin, String action, String operator, String content) {
        List<HashMap<String, String>> logs;
        if (StringUtils.isBlank(origin)) {
            logs = new ArrayList<>();
        } else {
            try {
                logs = GSON.fromJson(origin, new TypeToken<List<HashMap<String, String>>>() {
                }.getType());
            } catch (Exception e) {
                logs = new ArrayList<>();
            }
        }
        if (logs == null) {
            logs = new ArrayList<>();
        }
        HashMap<String, String> item = new HashMap<>();
        item.put("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        item.put("action", action == null ? "" : action);
        item.put("operator", operator == null ? "" : operator);
        item.put("content", content == null ? "" : content);
        logs.add(item);
        return GSON.toJson(logs);
    }
}
