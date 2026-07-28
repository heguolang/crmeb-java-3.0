/** 佣金记录 brokerageLevel 展示映射 */
export const BROKERAGE_LEVEL_SELF = 0;
export const BROKERAGE_LEVEL_ONE = 1;
export const BROKERAGE_LEVEL_TWO = 2;
export const BROKERAGE_LEVEL_TEAM_DIFF = 10;
export const BROKERAGE_LEVEL_TEAM_PEER = 11;

export function getBrokerageLevelLabel(level) {
  const map = {
    [BROKERAGE_LEVEL_SELF]: '自购',
    [BROKERAGE_LEVEL_ONE]: '一级',
    [BROKERAGE_LEVEL_TWO]: '二级',
    [BROKERAGE_LEVEL_TEAM_DIFF]: '社群奖',
    [BROKERAGE_LEVEL_TEAM_PEER]: '培育奖',
  };
  return map[level] || '';
}

/** H5 展示文案：仅前端替换，不改后端存库 */
export function formatBrokerageDisplayText(text) {
  if (text == null || text === '') return text;
  return String(text)
    .replace(/获得团队平级奖/g, '获得培育奖')
    .replace(/获得团队极差奖/g, '获得社群奖')
    .replace(/团队平级/g, '培育奖')
    .replace(/团队极差/g, '社群奖')
    .replace(/平级奖/g, '培育奖')
    .replace(/极差奖/g, '社群奖')
    .replace(/极差/g, '社群奖');
}

export function getBrokerageLevelClass(level) {
  const map = {
    [BROKERAGE_LEVEL_SELF]: 'tag-self',
    [BROKERAGE_LEVEL_ONE]: 'tag-one',
    [BROKERAGE_LEVEL_TWO]: 'tag-two',
    [BROKERAGE_LEVEL_TEAM_DIFF]: 'tag-team-diff',
    [BROKERAGE_LEVEL_TEAM_PEER]: 'tag-team-peer',
  };
  return map[level] || '';
}
