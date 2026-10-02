package com.qxkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.constants.SmsConstants;
import com.qxkj.common.model.svc.SvcCase;
import com.qxkj.common.model.svc.SvcContract;
import com.qxkj.common.model.svc.SvcProject;
import com.qxkj.common.model.svc.SvcProjectProgress;
import com.qxkj.common.model.user.User;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.SvcRequests;
import com.qxkj.common.utils.RedisUtil;
import com.qxkj.service.dao.SvcCaseDao;
import com.qxkj.service.dao.SvcContractDao;
import com.qxkj.service.dao.SvcProjectDao;
import com.qxkj.service.dao.SvcProjectProgressDao;
import com.qxkj.service.dao.UserDao;
import com.qxkj.service.service.SmsService;
import com.qxkj.service.service.SvcService;
import com.qxkj.service.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 企业服务实现（合同签约 + 项目进度 + 客户案例 + 服务设置）
 */
@Slf4j
@Service
public class SvcServiceImpl extends ServiceImpl<SvcProjectDao, SvcProject> implements SvcService {

    @Resource
    private SvcContractDao svcContractDao;

    @Resource
    private SvcProjectDao svcProjectDao;

    @Resource
    private SvcProjectProgressDao svcProjectProgressDao;

    @Resource
    private SvcCaseDao svcCaseDao;

    @Resource
    private UserDao userDao;

    @Resource
    private RedisUtil redisUtil;

    @Autowired
    private SmsService smsService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    // ==================== 私有辅助 ====================

    private String makeContractNo() {
        // 合同编号：HT + yyyyMMdd + 6 位随机数
        String date = cn.hutool.core.date.DateUtil.format(new Date(), "yyyyMMdd");
        return "HT" + date + cn.hutool.core.util.RandomUtil.randomNumbers(6);
    }

    private void fillContract(List<SvcContract> list) {
        if (list == null || list.isEmpty()) return;
        for (SvcContract c : list) {
            User user = userDao.selectById(c.getUid());
            if (user != null) {
                c.setNickname(user.getNickname());
                c.setPhone(user.getPhone());
            }
        }
    }

    private void fillProject(List<SvcProject> list, boolean withProgress) {
        if (list == null || list.isEmpty()) return;
        for (SvcProject p : list) {
            p.setStatusText(statusText(p.getStatus()));
            User user = userDao.selectById(p.getUid());
            if (user != null) {
                p.setNickname(user.getNickname());
                p.setPhone(user.getPhone());
            }
            if (withProgress) {
                p.setProgressList(progressListOf(p.getId()));
            }
        }
    }

    private String statusText(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 1: return "需求确认";
            case 2: return "开发中";
            case 3: return "待验收";
            case 4: return "已完成";
            case -1: return "已终止";
            default: return "";
        }
    }

    // ==================== 后台：合同 ====================

    @Override
    public CommonPage<SvcContract> adminContractList(Integer uid, String keywords, Integer status, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        LambdaQueryWrapper<SvcContract> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SvcContract::getIsDel, 0);
        if (uid != null && uid > 0) lqw.eq(SvcContract::getUid, uid);
        if (status != null) lqw.eq(SvcContract::getStatus, status);
        if (keywords != null && !keywords.trim().isEmpty()) {
            lqw.and(w -> w.like(SvcContract::getTitle, keywords.trim())
                    .or().like(SvcContract::getContractNo, keywords.trim()));
        }
        lqw.orderByDesc(SvcContract::getId);
        List<SvcContract> list = svcContractDao.selectList(lqw);
        fillContract(list);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public SvcContract adminContractInfo(Integer id) {
        SvcContract contract = svcContractDao.selectById(id);
        if (contract == null || contract.getIsDel() > 0) {
            throw new RuntimeException("合同不存在");
        }
        return contract;
    }

    @Override
    public Boolean saveContract(SvcRequests.ContractSaveRequest request) {
        if (request.getId() != null && request.getId() > 0) {
            SvcContract exist = svcContractDao.selectById(request.getId());
            if (exist == null || exist.getIsDel() > 0) {
                throw new RuntimeException("合同不存在");
            }
            exist.setUid(request.getUid());
            exist.setTitle(request.getTitle());
            exist.setAmount(request.getAmount() == null ? BigDecimal.ZERO : request.getAmount());
            if (request.getPartyType() != null) exist.setPartyType(request.getPartyType());
            exist.setPartyName(request.getPartyName() == null ? "" : request.getPartyName());
            exist.setAnnex(request.getAnnex() == null ? "" : request.getAnnex());
            exist.setAnnexName(request.getAnnexName() == null ? "" : request.getAnnexName());
            exist.setSignPage(request.getSignPage() == null ? 0 : request.getSignPage());
            exist.setSignPos(request.getSignPos() == null ? "center" : request.getSignPos());
            exist.setSealPos(request.getSealPos() == null ? "center" : request.getSealPos());
            exist.setContent(request.getContent() == null ? "" : request.getContent());
            exist.setRemark(request.getRemark() == null ? "" : request.getRemark());
            return svcContractDao.updateById(exist) > 0;
        }
        SvcContract contract = new SvcContract();
        contract.setContractNo(makeContractNo());
        contract.setUid(request.getUid());
        contract.setTitle(request.getTitle());
        contract.setAmount(request.getAmount() == null ? BigDecimal.ZERO : request.getAmount());
        contract.setPartyType(request.getPartyType() == null ? 2 : request.getPartyType());
        contract.setPartyName(request.getPartyName() == null ? "" : request.getPartyName());
        contract.setAnnex(request.getAnnex() == null ? "" : request.getAnnex());
        contract.setAnnexName(request.getAnnexName() == null ? "" : request.getAnnexName());
        contract.setSignPage(request.getSignPage() == null ? 0 : request.getSignPage());
        contract.setSignPos(request.getSignPos() == null ? "center" : request.getSignPos());
        contract.setSealPos(request.getSealPos() == null ? "center" : request.getSealPos());
        contract.setContent(request.getContent() == null ? "" : request.getContent());
        contract.setRemark(request.getRemark() == null ? "" : request.getRemark());
        contract.setStatus(SvcContract.STATUS_WAIT_SIGN);
        contract.setIsDel(0);
        return svcContractDao.insert(contract) > 0;
    }

    @Override
    public Boolean deleteContract(Integer id) {
        SvcContract exist = svcContractDao.selectById(id);
        if (exist == null || exist.getIsDel() > 0) {
            throw new RuntimeException("合同不存在");
        }
        exist.setIsDel(1);
        return svcContractDao.updateById(exist) > 0;
    }

    // ==================== 后台：项目 ====================

    @Override
    public CommonPage<SvcProject> adminProjectList(Integer uid, String keywords, Integer status, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        LambdaQueryWrapper<SvcProject> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SvcProject::getIsDel, 0);
        if (uid != null && uid > 0) lqw.eq(SvcProject::getUid, uid);
        if (status != null) lqw.eq(SvcProject::getStatus, status);
        if (keywords != null && !keywords.trim().isEmpty()) {
            lqw.and(w -> w.like(SvcProject::getName, keywords.trim())
                    .or().like(SvcProject::getManager, keywords.trim()));
        }
        lqw.orderByDesc(SvcProject::getSort).orderByDesc(SvcProject::getId);
        List<SvcProject> list = svcProjectDao.selectList(lqw);
        fillProject(list, false);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public Boolean saveProject(SvcRequests.ProjectSaveRequest request) {
        if (request.getId() != null && request.getId() > 0) {
            SvcProject exist = svcProjectDao.selectById(request.getId());
            if (exist == null || exist.getIsDel() > 0) {
                throw new RuntimeException("项目不存在");
            }
            exist.setUid(request.getUid());
            exist.setName(request.getName());
            exist.setType(request.getType() == null ? "" : request.getType());
            if (request.getStatus() != null) exist.setStatus(request.getStatus());
            exist.setStage(request.getStage() == null ? "" : request.getStage());
            exist.setManager(request.getManager() == null ? "" : request.getManager());
            exist.setCycle(request.getCycle() == null ? "" : request.getCycle());
            exist.setSort(request.getSort() == null ? 0 : request.getSort());
            return svcProjectDao.updateById(exist) > 0;
        }
        SvcProject project = new SvcProject();
        project.setUid(request.getUid());
        project.setName(request.getName());
        project.setType(request.getType() == null ? "" : request.getType());
        project.setStatus(request.getStatus() == null ? SvcProject.STATUS_DEMAND : request.getStatus());
        project.setPercent(0);
        project.setStage(request.getStage() == null ? "" : request.getStage());
        project.setManager(request.getManager() == null ? "" : request.getManager());
        project.setCycle(request.getCycle() == null ? "" : request.getCycle());
        project.setSort(request.getSort() == null ? 0 : request.getSort());
        project.setIsDel(0);
        return svcProjectDao.insert(project) > 0;
    }

    @Override
    public Boolean deleteProject(Integer id) {
        SvcProject exist = svcProjectDao.selectById(id);
        if (exist == null || exist.getIsDel() > 0) {
            throw new RuntimeException("项目不存在");
        }
        exist.setIsDel(1);
        return svcProjectDao.updateById(exist) > 0;
    }

    @Override
    public List<SvcProjectProgress> progressListOf(Integer projectId) {
        LambdaQueryWrapper<SvcProjectProgress> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SvcProjectProgress::getProjectId, projectId);
        lqw.orderByDesc(SvcProjectProgress::getId);
        return svcProjectProgressDao.selectList(lqw);
    }

    @Override
    public Boolean saveProgress(SvcRequests.ProgressSetRequest request) {
        SvcProject project = svcProjectDao.selectById(request.getProjectId());
        if (project == null || project.getIsDel() > 0) {
            throw new RuntimeException("项目不存在");
        }
        int percent = request.getPercent();
        if (percent < 0) percent = 0;
        if (percent > 100) percent = 100;
        final int finalPercent = percent;
        final SvcProject finalProject = project;

        return transactionTemplate.execute(status -> {
            SvcProjectProgress record = new SvcProjectProgress();
            record.setProjectId(finalProject.getId());
            record.setUid(finalProject.getUid());
            record.setStage(request.getStage());
            record.setPercent(finalPercent);
            record.setRemark(request.getRemark() == null ? "" : request.getRemark());
            svcProjectProgressDao.insert(record);

            finalProject.setPercent(finalPercent);
            finalProject.setStage(request.getStage());
            if (request.getSyncStatus() != null && request.getSyncStatus() == 1) {
                finalProject.setStatus(finalPercent >= 100 ? SvcProject.STATUS_WAIT_ACCEPT : SvcProject.STATUS_DEVELOPING);
            }
            svcProjectDao.updateById(finalProject);
            return true;
        });
    }

    // ==================== 后台：客户案例 ====================

    @Override
    public CommonPage<SvcCase> adminCaseList(String keywords, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        LambdaQueryWrapper<SvcCase> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SvcCase::getIsDel, 0);
        if (keywords != null && !keywords.trim().isEmpty()) {
            lqw.like(SvcCase::getTitle, keywords.trim());
        }
        lqw.orderByDesc(SvcCase::getSort).orderByDesc(SvcCase::getId);
        List<SvcCase> list = svcCaseDao.selectList(lqw);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public Boolean saveCase(SvcRequests.CaseSaveRequest request) {
        if (request.getId() != null && request.getId() > 0) {
            SvcCase exist = svcCaseDao.selectById(request.getId());
            if (exist == null || exist.getIsDel() > 0) {
                throw new RuntimeException("案例不存在");
            }
            exist.setTitle(request.getTitle());
            exist.setCategory(request.getCategory() == null ? "" : request.getCategory());
            exist.setDescription(request.getDescription() == null ? "" : request.getDescription());
            exist.setImage(request.getImage() == null ? "" : request.getImage());
            exist.setSort(request.getSort() == null ? 0 : request.getSort());
            if (request.getIsShow() != null) exist.setIsShow(request.getIsShow());
            return svcCaseDao.updateById(exist) > 0;
        }
        SvcCase svcCase = new SvcCase();
        svcCase.setTitle(request.getTitle());
        svcCase.setCategory(request.getCategory() == null ? "" : request.getCategory());
        svcCase.setDescription(request.getDescription() == null ? "" : request.getDescription());
        svcCase.setImage(request.getImage() == null ? "" : request.getImage());
        svcCase.setSort(request.getSort() == null ? 0 : request.getSort());
        svcCase.setIsShow(request.getIsShow() == null ? 1 : request.getIsShow());
        svcCase.setIsDel(0);
        return svcCaseDao.insert(svcCase) > 0;
    }

    @Override
    public Boolean deleteCase(Integer id) {
        SvcCase exist = svcCaseDao.selectById(id);
        if (exist == null || exist.getIsDel() > 0) {
            throw new RuntimeException("案例不存在");
        }
        exist.setIsDel(1);
        return svcCaseDao.updateById(exist) > 0;
    }

    // ==================== 后台：服务设置 ====================

    private static final String[] SETTING_KEYS = {
            "svc_home_mode", "svc_user_mode", "svc_nav_mode", "svc_sign_sms",
            "svc_company_name", "svc_contact_phone", "svc_contact_wechat", "svc_contact_time"
    };

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
        map.put("homeMode", "1".equals(systemConfigService.getValueByKey("svc_home_mode")));
        map.put("userMode", "1".equals(systemConfigService.getValueByKey("svc_user_mode")));
        map.put("navMode", "1".equals(systemConfigService.getValueByKey("svc_nav_mode")));
        map.put("companyName", systemConfigService.getValueByKey("svc_company_name"));
        map.put("contactPhone", systemConfigService.getValueByKey("svc_contact_phone"));
        map.put("contactWechat", systemConfigService.getValueByKey("svc_contact_wechat"));
        map.put("contactTime", systemConfigService.getValueByKey("svc_contact_time"));
        map.put("signSms", "1".equals(systemConfigService.getValueByKey("svc_sign_sms")));
        return map;
    }

    @Override
    public List<SvcCase> frontCaseList(Integer limit) {
        LambdaQueryWrapper<SvcCase> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SvcCase::getIsDel, 0);
        lqw.eq(SvcCase::getIsShow, 1);
        lqw.orderByDesc(SvcCase::getSort).orderByDesc(SvcCase::getId);
        if (limit != null && limit > 0) {
            lqw.last(" limit " + limit);
        }
        return svcCaseDao.selectList(lqw);
    }

    @Override
    public CommonPage<SvcContract> myContractList(Integer uid, Integer status, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        LambdaQueryWrapper<SvcContract> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SvcContract::getIsDel, 0);
        lqw.eq(SvcContract::getUid, uid);
        if (status != null) lqw.eq(SvcContract::getStatus, status);
        lqw.orderByDesc(SvcContract::getId);
        List<SvcContract> list = svcContractDao.selectList(lqw);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public SvcContract myContractDetail(Integer uid, Integer id) {
        SvcContract contract = svcContractDao.selectById(id);
        if (contract == null || contract.getIsDel() > 0 || !contract.getUid().equals(uid)) {
            throw new RuntimeException("合同不存在");
        }
        return contract;
    }

    @Override
    public Boolean signContract(Integer uid, SvcRequests.ContractSignRequest request) {
        SvcContract contract = svcContractDao.selectById(request.getId());
        if (contract == null || contract.getIsDel() > 0 || !contract.getUid().equals(uid)) {
            throw new RuntimeException("合同不存在");
        }
        if (!SvcContract.STATUS_WAIT_SIGN.equals(contract.getStatus())) {
            throw new RuntimeException("当前合同状态不可签约");
        }
        String signHand = request.getSignImage();
        if (signHand == null || signHand.trim().isEmpty()) {
            throw new RuntimeException("请手写签名");
        }
        // 企业签约必须带电子签章（样式 + 用主体名称生成的签章图）
        boolean isCompany = SvcContract.PARTY_COMPANY.equals(contract.getPartyType());
        if (isCompany && (request.getSealImage() == null || request.getSealImage().trim().isEmpty())) {
            throw new RuntimeException("请选择签章样式并生成电子签章");
        }

        return transactionTemplate.execute(status -> {
            contract.setStatus(SvcContract.STATUS_SIGNED);
            contract.setSignTime(new Date());
            contract.setSignName(request.getSignName() == null ? "" : request.getSignName().trim());
            contract.setSignPhone(request.getPhone() == null ? "" : request.getPhone().trim());
            contract.setSignHand(signHand.trim());
            if (isCompany) {
                contract.setSealStyle(normalizeSealStyle(request.getSealStyle()));
                contract.setSealImage(request.getSealImage().trim());
            } else {
                // 个人签约无需盖章：清掉签章字段，避免残留旧值
                contract.setSealStyle("");
                contract.setSealImage("");
            }
            // 独立签署页（签章 + 签字 + 签署日期）：把签署要素真正附着到合同上
            contract.setSignSheet(request.getSignSheet() == null ? "" : request.getSignSheet().trim());
            // 签约后自动生成并关联项目，会员端「我的项目」同步可见
            if (contract.getProjectId() == null
                    || svcProjectDao.selectById(contract.getProjectId()) == null
                    || svcProjectDao.selectById(contract.getProjectId()).getIsDel() > 0) {
                SvcProject project = new SvcProject();
                project.setUid(uid);
                project.setName(contract.getTitle());
                project.setType("");
                project.setStatus(SvcProject.STATUS_DEMAND);
                project.setPercent(0);
                project.setStage("需求确认");
                project.setIsDel(0);
                svcProjectDao.insert(project);
                contract.setProjectId(project.getId());
            }
            return svcContractDao.updateById(contract) > 0;
        });
    }

    /** 签章样式收敛到已知四款，未识别的一律按「圆形标准章」处理 */
    private String normalizeSealStyle(String style) {
        String s = style == null ? "" : style.trim().toLowerCase();
        if (SvcContract.SEAL_OVAL.equals(s) || SvcContract.SEAL_SQUARE.equals(s) || SvcContract.SEAL_MODERN.equals(s)) {
            return s;
        }
        return SvcContract.SEAL_ROUND;
    }

    @Override
    public Boolean sendSignCode(String phone) {
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号码错误");
        }
        return smsService.sendCommonCode(phone);
    }

    @Override
    public void checkSignCode(String phone, String code) {
        if (code == null || code.trim().isEmpty()) {
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

    @Override
    public CommonPage<SvcProject> myProjectList(Integer uid, Integer status, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        LambdaQueryWrapper<SvcProject> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SvcProject::getIsDel, 0);
        lqw.eq(SvcProject::getUid, uid);
        if (status != null) lqw.eq(SvcProject::getStatus, status);
        lqw.orderByDesc(SvcProject::getSort).orderByDesc(SvcProject::getId);
        List<SvcProject> list = svcProjectDao.selectList(lqw);
        fillProject(list, false);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public SvcProject myProjectDetail(Integer uid, Integer id) {
        SvcProject project = svcProjectDao.selectById(id);
        if (project == null || project.getIsDel() > 0 || !project.getUid().equals(uid)) {
            throw new RuntimeException("项目不存在");
        }
        fillProject(new ArrayList<SvcProject>() {{ add(project); }}, true);
        return project;
    }

    @Override
    public HashMap<String, Object> myStats(Integer uid) {
        HashMap<String, Object> map = new HashMap<>();
        LambdaQueryWrapper<SvcProject> doing = new LambdaQueryWrapper<>();
        doing.eq(SvcProject::getIsDel, 0);
        doing.eq(SvcProject::getUid, uid);
        doing.in(SvcProject::getStatus, SvcProject.STATUS_DEMAND, SvcProject.STATUS_DEVELOPING);
        map.put("doingCount", svcProjectDao.selectCount(doing));

        LambdaQueryWrapper<SvcProject> waitAccept = new LambdaQueryWrapper<>();
        waitAccept.eq(SvcProject::getIsDel, 0);
        waitAccept.eq(SvcProject::getUid, uid);
        waitAccept.eq(SvcProject::getStatus, SvcProject.STATUS_WAIT_ACCEPT);
        map.put("waitAcceptCount", svcProjectDao.selectCount(waitAccept));

        User user = userDao.selectById(uid);
        map.put("balance", user == null || user.getNowMoney() == null ? BigDecimal.ZERO : user.getNowMoney());
        return map;
    }

    @Override
    public List<SvcProjectProgress> myDynamic(Integer uid, Integer limit) {
        // 最新动态：我的项目最近进度记录
        LambdaQueryWrapper<SvcProject> plqw = new LambdaQueryWrapper<>();
        plqw.eq(SvcProject::getIsDel, 0);
        plqw.eq(SvcProject::getUid, uid);
        plqw.select(SvcProject::getId);
        List<SvcProject> projects = svcProjectDao.selectList(plqw);
        if (projects.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> ids = new ArrayList<>();
        for (SvcProject p : projects) {
            ids.add(p.getId());
        }
        LambdaQueryWrapper<SvcProjectProgress> lqw = new LambdaQueryWrapper<>();
        lqw.in(SvcProjectProgress::getProjectId, ids);
        lqw.orderByDesc(SvcProjectProgress::getId);
        if (limit != null && limit > 0) {
            lqw.last(" limit " + limit);
        }
        return svcProjectProgressDao.selectList(lqw);
    }
}
