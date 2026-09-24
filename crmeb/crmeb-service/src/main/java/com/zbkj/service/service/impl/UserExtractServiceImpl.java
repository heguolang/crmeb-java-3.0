package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.constants.BrokerageRecordConstants;
import com.zbkj.common.constants.Constants;
import com.zbkj.common.constants.SysConfigConstants;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.finance.UserExtract;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.user.UserBill;
import com.zbkj.common.model.user.UserBrokerageRecord;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.UserExtractRequest;
import com.zbkj.common.request.UserExtractSearchRequest;
import com.zbkj.common.response.BalanceResponse;
import com.zbkj.common.response.UserExtractRecordResponse;
import com.zbkj.common.response.UserExtractResponse;
import com.zbkj.common.utils.CrmebDateUtil;
import com.zbkj.common.vo.DateLimitUtilVo;
import com.zbkj.service.dao.UserExtractDao;
import com.zbkj.service.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ZERO;

/**
 * UserExtractServiceImpl 接口实现
 * +----------------------------------------------------------------------
 * | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
 * +----------------------------------------------------------------------
 * | Copyright (c) 2016~2024 https://www.crmeb.com All rights reserved.
 * +----------------------------------------------------------------------
 * | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
 * +----------------------------------------------------------------------
 * | Author: CRMEB Team <admin@crmeb.com>
 * +----------------------------------------------------------------------
 */
@Service
public class UserExtractServiceImpl extends ServiceImpl<UserExtractDao, UserExtract> implements UserExtractService {

    @Resource
    private UserExtractDao dao;

    @Autowired
    private UserService userService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private SystemAttachmentService systemAttachmentService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private UserBrokerageRecordService userBrokerageRecordService;

    @Autowired
    private UserBillService userBillService;


    /**
     * 列表
     *
     * @param request          请求参数
     * @param pageParamRequest 分页类参数
     * @return List<UserExtract>
     * @author Mr.Zhang
     * @since 2020-05-11
     */
    @Override
    public List<UserExtract> getList(UserExtractSearchRequest request, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());

        //带 UserExtract 类的多条件查询
        LambdaQueryWrapper<UserExtract> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isBlank(request.getKeywords())) {
            lambdaQueryWrapper.and(i -> i.
                    or().like(UserExtract::getWechat, request.getKeywords()).   //微信号
                            or().like(UserExtract::getRealName, request.getKeywords()). //名称
                            or().like(UserExtract::getBankCode, request.getKeywords()). //银行卡
                            or().like(UserExtract::getBankAddress, request.getKeywords()). //开户行
                            or().like(UserExtract::getAlipayCode, request.getKeywords()). //支付宝
                            or().like(UserExtract::getFailMsg, request.getKeywords()) //失败原因
            );
        }

        //提现状态
        if (request.getStatus() != null) {
            lambdaQueryWrapper.eq(UserExtract::getStatus, request.getStatus());
        }

        //提现方式
        if (!StringUtils.isBlank(request.getExtractType())) {
            lambdaQueryWrapper.eq(UserExtract::getExtractType, request.getExtractType());
        }

        //提现类别
        if (!StringUtils.isBlank(request.getExtractCategory())) {
            if (SysConfigConstants.EXTRACT_CATEGORY_BROKERAGE.equals(request.getExtractCategory())) {
                lambdaQueryWrapper.and(w -> w.eq(UserExtract::getExtractCategory, SysConfigConstants.EXTRACT_CATEGORY_BROKERAGE)
                        .or().isNull(UserExtract::getExtractCategory)
                        .or().eq(UserExtract::getExtractCategory, ""));
            } else {
                lambdaQueryWrapper.eq(UserExtract::getExtractCategory, request.getExtractCategory());
            }
        }

        //时间范围
        if (StringUtils.isNotBlank(request.getDateLimit())) {
            DateLimitUtilVo dateLimit = CrmebDateUtil.getDateLimit(request.getDateLimit());
            lambdaQueryWrapper.between(UserExtract::getCreateTime, dateLimit.getStartTime(), dateLimit.getEndTime());
        }

        //按创建时间降序排列
        lambdaQueryWrapper.orderByDesc(UserExtract::getCreateTime, UserExtract::getId);

        List<UserExtract> extractList = dao.selectList(lambdaQueryWrapper);
        if (CollUtil.isEmpty(extractList)) {
            return extractList;
        }
        List<Integer> uidList = extractList.stream().map(UserExtract::getUid).distinct().collect(Collectors.toList());
        HashMap<Integer, User> userMap = userService.getMapListInUid(uidList);
        for (UserExtract userExtract : extractList) {
            User user = userMap.get(userExtract.getUid());
            String nickName = user != null ? Optional.ofNullable(user.getNickname()).orElse("") : "";
            userExtract.setNickName(nickName);
            if (StrUtil.isBlank(userExtract.getExtractCategory())) {
                userExtract.setExtractCategory(SysConfigConstants.EXTRACT_CATEGORY_BROKERAGE);
            }
        }
        return extractList;
    }

    /**
     * 提现总金额
     * 总佣金 = 已提现佣金 + 未提现佣金
     * 已提现佣金 = 用户成功提现的金额
     * 未提现佣金 = 用户未提现的佣金 = 可提现佣金 + 冻结佣金 = 用户佣金
     * 可提现佣金 = 包括解冻佣金、提现未通过的佣金 = 用户佣金 - 冻结期佣金
     * 待提现佣金 = 待审核状态的佣金
     * 冻结佣金 = 用户在冻结期的佣金，不包括退回佣金
     * 退回佣金 = 因退款导致的冻结佣金退回
     */
    @Override
    public BalanceResponse getBalance(String dateLimit) {
        String startTime = "";
        String endTime = "";
        if (StringUtils.isNotBlank(dateLimit)) {
            DateLimitUtilVo dateRage = CrmebDateUtil.getDateLimit(dateLimit);
            startTime = dateRage.getStartTime();
            endTime = dateRage.getEndTime();
        }

        // 已提现
        BigDecimal withdrawn = getWithdrawn(startTime, endTime);
        // 待提现(审核中)
        BigDecimal toBeWithdrawn = getWithdrawning(startTime, endTime);

        // 佣金总金额（单位时间）
        BigDecimal commissionTotal = userBrokerageRecordService.getTotalSpreadPriceBydateLimit(dateLimit);
        // 单位时间消耗的佣金
        BigDecimal subWithdarw = userBrokerageRecordService.getSubSpreadPriceByDateLimit(dateLimit);
        // 未提现
        BigDecimal unDrawn = commissionTotal.subtract(subWithdarw);
        return new BalanceResponse(withdrawn, unDrawn, commissionTotal, toBeWithdrawn);
    }


    /**
     * 提现总金额
     *
     * @return BalanceResponse
     * @author Mr.Zhang
     * @since 2020-05-11
     */
    @Override
    public BigDecimal getWithdrawn(String startTime, String endTime) {
        return getSum(null, 1, startTime, endTime);
    }

    /**
     * 审核中总金额
     *
     * @return BalanceResponse
     * @author Mr.Zhang
     * @since 2020-05-11
     */
    private BigDecimal getWithdrawning(String startTime, String endTime) {
        return getSum(null, 0, startTime, endTime);
    }

    /**
     * 根据状态获取总额
     *
     * @return BigDecimal
     */
    private BigDecimal getSum(Integer userId, int status, String startTime, String endTime) {
        LambdaQueryWrapper<UserExtract> lqw = Wrappers.lambdaQuery();
        if (null != userId) {
            lqw.eq(UserExtract::getUid, userId);
        }
        lqw.eq(UserExtract::getStatus, status);
        if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
            lqw.between(UserExtract::getCreateTime, startTime, endTime);
        }
        List<UserExtract> userExtracts = dao.selectList(lqw);
        BigDecimal sum = ZERO;
        if (CollUtil.isNotEmpty(userExtracts)) {
            sum = userExtracts.stream().map(UserExtract::getExtractPrice).reduce(ZERO, BigDecimal::add);
        }
        return sum;
    }

    /**
     * 获取用户对应的提现数据
     *
     * @param userId 用户id
     * @return 提现数据
     */
    @Override
    public UserExtractResponse getUserExtractByUserId(Integer userId) {
        QueryWrapper<UserExtract> qw = new QueryWrapper<>();
        qw.select("SUM(extract_price) as extract_price,count(id) as id, uid");
        qw.ge("status", 1);
        qw.eq("uid", userId);
        qw.groupBy("uid");
        UserExtract ux = dao.selectOne(qw);
        UserExtractResponse uexr = new UserExtractResponse();
//        uexr.setEuid(ux.getUid());
        if (null != ux) {
            uexr.setExtractCountNum(ux.getId()); // 这里的id其实是数量，借变量传递
            uexr.setExtractCountPrice(ux.getExtractPrice());
        } else {
            uexr.setExtractCountNum(0); // 这里的id其实是数量，借变量传递
            uexr.setExtractCountPrice(ZERO);
        }

        return uexr;
    }

    /**
     * 提现审核
     *
     * @param id          提现申请id
     * @param status      审核状态 -1 未通过 0 审核中 1 已提现
     * @param backMessage 驳回原因
     * @return 审核结果
     */
    @Override
    public Boolean updateStatus(Integer id, Integer status, String backMessage) {
        if (status == -1 && StringUtils.isBlank(backMessage))
            throw new CrmebException("驳回时请填写驳回原因");

        UserExtract userExtract = getById(id);
        if (ObjectUtil.isNull(userExtract)) {
            throw new CrmebException("提现申请记录不存在");
        }
        if (userExtract.getStatus() != 0) {
            throw new CrmebException("提现申请已处理过");
        }
        userExtract.setStatus(status);

        User user = userService.getById(userExtract.getUid());
        if (ObjectUtil.isNull(user)) {
            throw new CrmebException("提现用户数据异常");
        }

        boolean isBalance = SysConfigConstants.EXTRACT_CATEGORY_BALANCE.equals(userExtract.getExtractCategory());
        Boolean execute = false;

        userExtract.setUpdateTime(cn.hutool.core.date.DateUtil.date());
        // 拒绝
        if (status == -1) {
            userExtract.setFailMsg(backMessage);
            if (isBalance) {
                UserBill userBill = new UserBill();
                userBill.setUid(user.getUid());
                userBill.setLinkId(userExtract.getId().toString());
                userBill.setPm(1);
                userBill.setTitle("余额提现退回");
                userBill.setCategory(Constants.USER_BILL_CATEGORY_MONEY);
                userBill.setType(Constants.USER_BILL_TYPE_SYSTEM_ADD);
                userBill.setNumber(userExtract.getExtractPrice());
                userBill.setBalance(user.getNowMoney().add(userExtract.getExtractPrice()));
                userBill.setMark(StrUtil.format("余额提现申请拒绝返还{}元", userExtract.getExtractPrice()));
                userBill.setStatus(1);
                userBill.setCreateTime(CrmebDateUtil.nowDateTime());

                execute = transactionTemplate.execute(e -> {
                    userService.operationNowMoney(userExtract.getUid(), userExtract.getExtractPrice(), user.getNowMoney(), "add");
                    userExtract.setUpdateTime(DateUtil.date());
                    updateById(userExtract);
                    userBillService.save(userBill);
                    return Boolean.TRUE;
                });
            } else {
                UserBrokerageRecord brokerageRecord = new UserBrokerageRecord();
                brokerageRecord.setUid(user.getUid());
                brokerageRecord.setLinkId(userExtract.getId().toString());
                brokerageRecord.setLinkType(BrokerageRecordConstants.BROKERAGE_RECORD_LINK_TYPE_WITHDRAW);
                brokerageRecord.setType(BrokerageRecordConstants.BROKERAGE_RECORD_TYPE_ADD);
                brokerageRecord.setTitle(BrokerageRecordConstants.BROKERAGE_RECORD_TITLE_WITHDRAW_FAIL);
                brokerageRecord.setPrice(userExtract.getExtractPrice());
                brokerageRecord.setBalance(user.getBrokeragePrice().add(userExtract.getExtractPrice()));
                brokerageRecord.setMark(StrUtil.format("提现申请拒绝返还佣金{}", userExtract.getExtractPrice()));
                brokerageRecord.setStatus(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_COMPLETE);
                brokerageRecord.setCreateTime(CrmebDateUtil.nowDateTime());

                execute = transactionTemplate.execute(e -> {
                    userService.operationBrokerage(userExtract.getUid(), userExtract.getExtractPrice(), user.getBrokeragePrice(), "add");
                    userExtract.setUpdateTime(DateUtil.date());
                    updateById(userExtract);
                    userBrokerageRecordService.save(brokerageRecord);
                    return Boolean.TRUE;
                });
            }
        }

        // 同意
        if (status == 1) {
            if (isBalance) {
                execute = transactionTemplate.execute(e -> {
                    userExtract.setUpdateTime(DateUtil.date());
                    updateById(userExtract);
                    return Boolean.TRUE;
                });
            } else {
                UserBrokerageRecord brokerageRecord = userBrokerageRecordService.getByLinkIdAndLinkType(userExtract.getId().toString(), BrokerageRecordConstants.BROKERAGE_RECORD_LINK_TYPE_WITHDRAW);
                if (ObjectUtil.isNull(brokerageRecord)) {
                    throw new CrmebException("对应的佣金记录不存在");
                }
                execute = transactionTemplate.execute(e -> {
                    userExtract.setUpdateTime(DateUtil.date());
                    updateById(userExtract);
                    brokerageRecord.setStatus(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_COMPLETE);
                    brokerageRecord.setUpdateTime(DateUtil.date());
                    userBrokerageRecordService.updateById(brokerageRecord);
                    return Boolean.TRUE;
                });
            }
        }
        return execute;
    }

    /**
     * 获取提现记录列表
     *
     * @param userId           用户uid
     * @param pageParamRequest 分页参数
     * @return PageInfo
     */
    @Override
    public PageInfo<UserExtractRecordResponse> getExtractRecord(Integer userId, PageParamRequest pageParamRequest) {
        Page<UserExtract> userExtractPage = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        QueryWrapper<UserExtract> queryWrapper = new QueryWrapper<>();
        queryWrapper.select( "ANY_VALUE(create_time) as create_time");
        queryWrapper.eq("uid", userId);
        queryWrapper.groupBy("left(create_time, 7)");
        queryWrapper.orderByDesc("left(create_time, 7)");
        List<UserExtract> list = dao.selectList(queryWrapper);
        if (CollUtil.isEmpty(list)) {
            return new PageInfo<>();
        }
        ArrayList<UserExtractRecordResponse> userExtractRecordResponseList = CollectionUtil.newArrayList();
        for (UserExtract userExtract : list) {
            String date = CrmebDateUtil.dateToStr(userExtract.getCreateTime(), Constants.DATE_FORMAT_MONTH);
            userExtractRecordResponseList.add(new UserExtractRecordResponse(date, getListByMonth(userId, date)));
        }

        return CommonPage.copyPageInfo(userExtractPage, userExtractRecordResponseList);
    }

    private List<UserExtract> getListByMonth(Integer userId, String date) {
        QueryWrapper<UserExtract> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "extract_price", "fail_msg", "status", "create_time", "update_time");
        queryWrapper.eq("uid", userId);
        queryWrapper.apply(StrUtil.format(" left(create_time, 7) = '{}'", date));
        queryWrapper.orderByDesc("create_time");
        return dao.selectList(queryWrapper);
    }

    /**
     * 获取用户提现总金额
     *
     * @param userId 用户uid
     * @return BigDecimal
     */
    @Override
    public BigDecimal getExtractTotalMoney(Integer userId) {
        return getSum(userId, 1, null, null);
    }


    /**
     * 提现申请
     *
     * @return Boolean
     */
    @Override
    public Boolean extractApply(UserExtractRequest request) {
        String category = StrUtil.blankToDefault(request.getExtractCategory(), SysConfigConstants.EXTRACT_CATEGORY_BROKERAGE);
        boolean isBalance = SysConfigConstants.EXTRACT_CATEGORY_BALANCE.equals(category);

        // 功能开关
        String switchKey = isBalance ? SysConfigConstants.CONFIG_BALANCE_EXTRACT_SWITCH : SysConfigConstants.CONFIG_EXTRACT_SWITCH;
        String extractSwitch = systemConfigService.getValueByKey(switchKey);
        if (StrUtil.isNotBlank(extractSwitch) && !"1".equals(extractSwitch)) {
            throw new CrmebException(isBalance ? "余额提现功能已关闭" : "佣金提现功能已关闭");
        }
        // 可提现时间校验
        checkExtractTimeAllowed(true, category);

        // 最低提现金额
        String minKey = isBalance ? SysConfigConstants.CONFIG_BALANCE_EXTRACT_MIN_PRICE : Constants.CONFIG_EXTRACT_MIN_PRICE;
        String value = systemConfigService.getValueByKey(minKey);
        if (StrUtil.isBlank(value)) {
            value = "1";
        }
        BigDecimal ten = new BigDecimal(value);
        if (request.getExtractPrice().compareTo(ten) < 0) {
            throw new CrmebException(StrUtil.format("最低提现金额{}元", ten));
        }

        // 提现倍数校验，0不限制
        String multipleKey = isBalance ? SysConfigConstants.CONFIG_BALANCE_EXTRACT_MULTIPLE : SysConfigConstants.CONFIG_EXTRACT_MULTIPLE;
        String multipleStr = systemConfigService.getValueByKey(multipleKey);
        if (StrUtil.isNotBlank(multipleStr)) {
            BigDecimal multiple = new BigDecimal(multipleStr);
            if (multiple.compareTo(ZERO) > 0) {
                BigDecimal[] divRem = request.getExtractPrice().divideAndRemainder(multiple);
                if (divRem[1].compareTo(ZERO) != 0) {
                    throw new CrmebException(StrUtil.format("提现金额须为{}的倍数", multiple.stripTrailingZeros().toPlainString()));
                }
            }
        }

        // 银行卡时校验银行是否在支持列表中
        if ("bank".equals(request.getExtractType()) && StrUtil.isNotBlank(request.getBankName())) {
            List<String> banks = getSupportBankList();
            if (CollUtil.isNotEmpty(banks) && !banks.contains(request.getBankName().trim())) {
                throw new CrmebException("不支持的提现银行：" + request.getBankName());
            }
        }

        User user = userService.getInfo();
        if (ObjectUtil.isNull(user)) {
            throw new CrmebException("提现用户信息异常");
        }

        BigDecimal money = isBalance ? user.getNowMoney() : user.getBrokeragePrice();
        if (money.compareTo(ZERO) < 1) {
            throw new CrmebException("您当前没有金额可以提现");
        }
        if (money.compareTo(request.getExtractPrice()) < 0) {
            throw new CrmebException("你当前最多可提现 " + money + "元");
        }

        BigDecimal fee = calcExtractFee(request.getExtractPrice(), category);
        BigDecimal arrivePrice = request.getExtractPrice().subtract(fee);
        if (arrivePrice.compareTo(ZERO) < 0) {
            arrivePrice = ZERO;
        }

        UserExtract userExtract = new UserExtract();
        BeanUtils.copyProperties(request, userExtract);
        userExtract.setUid(user.getUid());
        userExtract.setBalance(money.subtract(request.getExtractPrice()));
        userExtract.setExtractCategory(category);
        if (StrUtil.isNotBlank(userExtract.getQrcodeUrl())) {
            userExtract.setQrcodeUrl(systemAttachmentService.clearPrefix(userExtract.getQrcodeUrl()));
        }
        if (fee.compareTo(ZERO) > 0) {
            String feeMark = StrUtil.format("手续费{}元，预计到账{}元", fee, arrivePrice);
            userExtract.setMark(StrUtil.isBlank(userExtract.getMark()) ? feeMark : userExtract.getMark() + "；" + feeMark);
        }

        if (isBalance) {
            UserBill userBill = new UserBill();
            userBill.setUid(user.getUid());
            userBill.setPm(0);
            userBill.setTitle("余额提现");
            userBill.setCategory(Constants.USER_BILL_CATEGORY_MONEY);
            userBill.setType(Constants.USER_BILL_TYPE_EXTRACT);
            userBill.setNumber(userExtract.getExtractPrice());
            userBill.setBalance(money.subtract(userExtract.getExtractPrice()));
            userBill.setMark(StrUtil.format("余额提现申请扣除{}元", userExtract.getExtractPrice()));
            userBill.setStatus(1);
            userBill.setCreateTime(CrmebDateUtil.nowDateTime());

            return transactionTemplate.execute(e -> {
                save(userExtract);
                userService.operationNowMoney(user.getUid(), userExtract.getExtractPrice(), money, "sub");
                userBill.setLinkId(userExtract.getId().toString());
                userBillService.save(userBill);
                return Boolean.TRUE;
            });
        }

        UserBrokerageRecord brokerageRecord = new UserBrokerageRecord();
        brokerageRecord.setUid(user.getUid());
        brokerageRecord.setLinkType(BrokerageRecordConstants.BROKERAGE_RECORD_LINK_TYPE_WITHDRAW);
        brokerageRecord.setType(BrokerageRecordConstants.BROKERAGE_RECORD_TYPE_SUB);
        brokerageRecord.setTitle(BrokerageRecordConstants.BROKERAGE_RECORD_TITLE_WITHDRAW_APPLY);
        brokerageRecord.setPrice(userExtract.getExtractPrice());
        brokerageRecord.setBalance(money.subtract(userExtract.getExtractPrice()));
        brokerageRecord.setMark(StrUtil.format("提现申请扣除佣金{}", userExtract.getExtractPrice()));
        brokerageRecord.setStatus(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_WITHDRAW);
        brokerageRecord.setCreateTime(CrmebDateUtil.nowDateTime());

        return transactionTemplate.execute(e -> {
            save(userExtract);
            userService.operationBrokerage(user.getUid(), userExtract.getExtractPrice(), money, "sub");
            brokerageRecord.setLinkId(userExtract.getId().toString());
            userBrokerageRecordService.save(brokerageRecord);
            return Boolean.TRUE;
        });
    }

    /**
     * 计算提现手续费
     */
    public BigDecimal calcExtractFee(BigDecimal extractPrice) {
        return calcExtractFee(extractPrice, SysConfigConstants.EXTRACT_CATEGORY_BROKERAGE);
    }

    public BigDecimal calcExtractFee(BigDecimal extractPrice, String category) {
        boolean isBalance = SysConfigConstants.EXTRACT_CATEGORY_BALANCE.equals(category);
        String feeType = systemConfigService.getValueByKey(isBalance
                ? SysConfigConstants.CONFIG_BALANCE_EXTRACT_FEE_TYPE
                : SysConfigConstants.CONFIG_EXTRACT_FEE_TYPE);
        String feeVal = systemConfigService.getValueByKey(isBalance
                ? SysConfigConstants.CONFIG_BALANCE_EXTRACT_FEE
                : SysConfigConstants.CONFIG_EXTRACT_FEE);
        if (StrUtil.isBlank(feeVal)) {
            return ZERO;
        }
        BigDecimal feeConfig = new BigDecimal(feeVal);
        if (feeConfig.compareTo(ZERO) <= 0) {
            return ZERO;
        }
        if ("fixed".equals(feeType)) {
            return feeConfig.setScale(2, RoundingMode.HALF_UP);
        }
        return extractPrice.multiply(feeConfig).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    /**
     * 校验当前是否在可提现时间；返回是否允许
     * @param throwEx true时不在窗口直接抛异常
     */
    @Override
    public boolean checkExtractTimeAllowed(boolean throwEx) {
        return checkExtractTimeAllowed(throwEx, SysConfigConstants.EXTRACT_CATEGORY_BROKERAGE);
    }

    @Override
    public boolean checkExtractTimeAllowed(boolean throwEx, String category) {
        boolean isBalance = SysConfigConstants.EXTRACT_CATEGORY_BALANCE.equals(category);
        String weekdays = systemConfigService.getValueByKey(isBalance
                ? SysConfigConstants.CONFIG_BALANCE_EXTRACT_WEEKDAYS
                : SysConfigConstants.CONFIG_EXTRACT_WEEKDAYS);
        String startStr = systemConfigService.getValueByKey(isBalance
                ? SysConfigConstants.CONFIG_BALANCE_EXTRACT_TIME_START
                : SysConfigConstants.CONFIG_EXTRACT_TIME_START);
        String endStr = systemConfigService.getValueByKey(isBalance
                ? SysConfigConstants.CONFIG_BALANCE_EXTRACT_TIME_END
                : SysConfigConstants.CONFIG_EXTRACT_TIME_END);

        LocalDateTime now = LocalDateTime.now();
        int day = now.getDayOfWeek().getValue(); // 1=周一 ... 7=周日
        if (StrUtil.isNotBlank(weekdays)) {
            Set<String> daySet = new HashSet<>(Arrays.asList(weekdays.split(",")));
            if (!daySet.contains(String.valueOf(day))) {
                if (throwEx) {
                    throw new CrmebException("当前不在可提现日，可提现：" + formatWeekdaysTip(weekdays));
                }
                return false;
            }
        }

        int startHour = parseHour(startStr, 0);
        int endHour = parseHour(endStr, 24);
        if (startHour < 0) startHour = 0;
        if (endHour > 24) endHour = 24;
        if (startHour >= endHour) {
            return true;
        }
        int hour = now.getHour();
        boolean inRange = hour >= startHour && (endHour == 24 || hour < endHour);
        if (!inRange) {
            if (throwEx) {
                throw new CrmebException("当前不在可提现时间，可提现：" + formatTimeTip(startHour, endHour));
            }
            return false;
        }
        return true;
    }

    /**
     * 支持银行列表（换行分隔配置）
     */
    public List<String> getSupportBankList() {
        String bank = systemConfigService.getValueByKey(Constants.CONFIG_BANK_LIST);
        if (StrUtil.isBlank(bank)) {
            return new ArrayList<>();
        }
        bank = bank.replace("\\n", "\n").replace("\r\n", "\n");
        List<String> bankArr = new ArrayList<>();
        for (String line : bank.split("\n")) {
            String t = line.trim();
            if (StrUtil.isNotBlank(t)) {
                bankArr.add(t);
            }
        }
        return bankArr;
    }

    public String formatWeekdaysTip(String weekdays) {
        if (StrUtil.isBlank(weekdays)) {
            return "周一至周日";
        }
        String[] names = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        List<String> list = new ArrayList<>();
        for (String d : weekdays.split(",")) {
            String t = d.trim();
            if (StrUtil.isBlank(t)) continue;
            try {
                int idx = Integer.parseInt(t);
                if (idx >= 1 && idx <= 7) list.add(names[idx]);
            } catch (Exception ignored) {
            }
        }
        return list.isEmpty() ? "周一至周日" : String.join("、", list);
    }

    public String formatTimeTip(int startHour, int endHour) {
        return String.format("%02d:00-%02d:00", startHour, endHour == 24 ? 24 : endHour);
    }

    private int parseHour(String val, int defaultVal) {
        if (StrUtil.isBlank(val)) {
            return defaultVal;
        }
        try {
            if (val.contains(":")) {
                return Integer.parseInt(val.split(":")[0]);
            }
            return Integer.parseInt(val.trim());
        } catch (Exception e) {
            return defaultVal;
        }
    }

    /**
     * 修改提现申请
     *
     * @param id                 申请id
     * @param userExtractRequest 具体参数
     */
    @Override
    public Boolean updateExtract(Integer id, UserExtractRequest userExtractRequest) {
        UserExtract userExtract = new UserExtract();
        BeanUtils.copyProperties(userExtractRequest, userExtract);
        userExtract.setId(id);
        userExtract.setUpdateTime(DateUtil.date());
        return updateById(userExtract);
    }

    /**
     * 提现申请待审核数量
     *
     * @return Integer
     */
    @Override
    public Integer getNotAuditNum() {
        LambdaQueryWrapper<UserExtract> lqw = Wrappers.lambdaQuery();
        lqw.eq(UserExtract::getStatus, 0);
        return dao.selectCount(lqw);
    }
}

