import { Message } from 'element-ui';
import exportExcel from '@/utils/newToExcel';

/**
 * 拉取列表全部页并导出 Excel
 * @param {Object} options
 * @param {Function} options.apiFn 列表接口
 * @param {Object} options.params 查询参数（不含分页）
 * @param {String} options.dateLimit 时间范围 "yyyy-MM-dd,yyyy-MM-dd"，可空
 * @param {String} options.clientDateField 客户端按该字段过滤日期
 * @param {String} options.filename 导出文件名
 * @param {Array} options.header 表头
 * @param {Array} options.filterVal 字段名
 * @param {Function} [options.mapRow] 行映射
 * @returns {Promise<boolean>}
 */
export async function runListExport(options = {}) {
  const {
    apiFn,
    params = {},
    dateLimit = '',
    clientDateField = '',
    filename = '导出数据',
    header = [],
    filterVal = [],
    mapRow,
  } = options;

  if (typeof apiFn !== 'function') {
    Message.error('导出接口未配置');
    return false;
  }

  try {
    const pageSize = 100;
    let page = 1;
    let allRows = [];
    let total = Infinity;

    while (allRows.length < total) {
      const res = await apiFn({
        ...params,
        page,
        limit: pageSize,
      });
      const parsed = normalizeListResult(res);
      if (!parsed.data.length) break;
      allRows = allRows.concat(parsed.data);
      total = parsed.total || allRows.length;
      if (parsed.data.length < pageSize) break;
      page += 1;
      if (page > 500) break;
    }

    if (dateLimit && clientDateField) {
      const [start, end] = dateLimit.split(',');
      const startTime = start ? new Date(`${start} 00:00:00`).getTime() : null;
      const endTime = end ? new Date(`${end} 23:59:59`).getTime() : null;
      allRows = allRows.filter((row) => {
        const raw = row[clientDateField];
        if (!raw) return false;
        const t = new Date(raw).getTime();
        if (Number.isNaN(t)) return false;
        if (startTime != null && t < startTime) return false;
        if (endTime != null && t > endTime) return false;
        return true;
      });
    }

    if (!allRows.length) {
      Message.warning('暂无导出数据');
      return false;
    }

    const tableData = typeof mapRow === 'function' ? allRows.map(mapRow) : allRows;
    exportExcel(header, filterVal, filename, tableData);
    Message.success(`导出成功，共 ${tableData.length} 条`);
    return true;
  } catch (e) {
    console.error(e);
    Message.error('导出失败');
    return false;
  }
}

function normalizeListResult(res) {
  if (Array.isArray(res)) {
    return { data: res, total: res.length };
  }
  if (res && Array.isArray(res.list)) {
    return { data: res.list, total: Number(res.total || 0) };
  }
  if (res && Array.isArray(res.data)) {
    return { data: res.data, total: Number(res.total || res.data.length || 0) };
  }
  return { data: [], total: 0 };
}
