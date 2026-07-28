/**
 * 佣金记录 brokerageLevel 展示映射
 * 0=自购 1=一级推广 2=二级推广 10=团队极差 11=团队平级
 */
export const BROKERAGE_LEVEL_SELF = 0;
export const BROKERAGE_LEVEL_ONE = 1;
export const BROKERAGE_LEVEL_TWO = 2;
export const BROKERAGE_LEVEL_TEAM_DIFF = 10;
export const BROKERAGE_LEVEL_TEAM_PEER = 11;

export const brokerageLevelOptions = [
  { value: 1, label: '一级推广' },
  { value: 2, label: '二级推广' },
  { value: 0, label: '自购返佣' },
  { value: 10, label: '团队极差奖' },
  { value: 11, label: '团队平级奖' },
];

export function getBrokerageLevelLabel(level) {
  const map = {
    [BROKERAGE_LEVEL_SELF]: '自购返佣',
    [BROKERAGE_LEVEL_ONE]: '一级推广',
    [BROKERAGE_LEVEL_TWO]: '二级推广',
    [BROKERAGE_LEVEL_TEAM_DIFF]: '团队极差奖',
    [BROKERAGE_LEVEL_TEAM_PEER]: '团队平级奖',
  };
  if (level === null || level === undefined || level === '') {
    return '推广返佣';
  }
  return map[level] || '推广返佣';
}

export function getBrokerageLevelTagType(level) {
  const map = {
    [BROKERAGE_LEVEL_SELF]: 'info',
    [BROKERAGE_LEVEL_ONE]: '',
    [BROKERAGE_LEVEL_TWO]: 'success',
    [BROKERAGE_LEVEL_TEAM_DIFF]: 'warning',
    [BROKERAGE_LEVEL_TEAM_PEER]: 'danger',
  };
  return map[level] || '';
}
