import os, re

BASE = r"D:\server\cc\LM-Server\src\main\java\com\lmserver\entity"

COL_CN = {
    'id': '主键ID',
    'username': '用户名',
    'password': '密码(BCrypt哈希)',
    'role': '角色: developer/admin/viewer/user/hidden',
    'display_name': '显示名称',
    'custom_name': '自定义名称',
    'email': '邮箱',
    'telegram_username': 'Telegram用户名',
    'platform': '所属平台: gg/fb',
    'config': '配置JSON',
    'token_version': 'JWT Token版本号(改密/禁用时递增)',
    'created_at': '创建时间',
    'updated_at': '更新时间',
    'last_login': '最后登录时间',
    'created_by': '创建者用户ID',
    'name': '名称',
    'account_id': '广告账户ID',
    'account_name': '账户名称',
    'mcc_id': 'MCC ID',
    'parent_mcc_id': '父MCC ID',
    'level_id': 'MCC等级ID',
    'shared_user_ids': '共享用户ID列表(JSON)',
    'timezone': '时区',
    'acquired_date': '获取日期',
    'death_date': '死亡日期',
    'status_changed_date': '状态变更日期',
    'owner_id': '归属用户ID',
    'deleted_at': '软删除时间',
    'agent_id': '代理ID',
    'status_id': '状态ID',
    'status': '状态',
    'product_name': '产品名称',
    'kpi': 'KPI指标',
    'region': '地区',
    'customer': '客户名称',
    'sales_person_id': '商务人员ID',
    'sales_person': '商务人员',
    'agency_ratio': '代理比例',
    'runner_ids': '在跑人员ID列表(JSON)',
    'is_archived': '是否归档: 0否/1是',
    'is_public': '是否公开: 0否/1是',
    'series_name': '系列名称',
    'package_name': '包名称',
    'url': 'URL地址',
    'link': '链接地址',
    'note': '备注',
    'bm_id': 'BM ID',
    'pixel_bm_id': 'Pixel BM ID',
    'pixel_name': 'Pixel名称',
    'pixel_id': 'Pixel ID',
    'product_id': '产品ID',
    'user_id': '用户ID',
    'video_id': '视频ID',
    'video_owner_id': '视频归属用户ID',
    'line_name': '线路/落地页名称',
    'content': '内容',
    'effectiveness': '成效标记',
    'frame_type': '融帧类型',
    'review_status': '审核状态',
    'channel_name': '频道名称',
    'imported_at': '导入时间',
    'amount': '金额',
    'operator': '操作员',
    'sheets_synced': 'Sheets同步标记: 0未同步/1已同步',
    'sheets_error': 'Sheets同步错误信息',
    'report_date': '报告日期',
    'report_to_client': '报给客户金额',
    'campaign': '广告系列',
    'cost': '消耗',
    'impressions': '展示次数',
    'clicks': '点击次数',
    'installs': '安装数',
    'in_app_actions': '应用内操作数',
    'cost_per_in_app': '单次应用内操作成本',
    'registrations': '注册数',
    'purchases': '购买数',
    'cost_per_purchase': '单次购买成本',
    'spreadsheet_id': 'Google Sheets表格ID',
    'sheet_gid': 'Sheet GID',
    'error_msg': '错误信息',
    'retry_count': '重试次数',
    'rows_json': '行数据JSON',
    'image_count': '图片数量',
    'saved_path': '保存路径',
    'logo_path': 'Logo路径',
    'last_scraped': '最后爬取时间',
    'scraped_by': '爬取操作人ID',
    'task_id': '任务唯一标识',
    'pkg': '包名称',
    'progress': '进度(0~1)',
    'message': '状态信息',
    'output_path': '输出路径',
    'output_name': '输出文件名',
    'finished_at': '完成时间',
    'settings': '设置JSON',
    'video_name': '视频文件名',
    'audio_name': '音频文件名',
    'size_mb': '文件大小(MB)',
    'is_delisted': '是否掉包: 0否/1是',
    'checked_at': '检测时间',
    'first_notified': '是否已首次通知',
    'dismissed_at': '关闭时间',
    'reminder_count': '提醒次数',
    'action': '操作类型',
    'target_type': '目标类型',
    'target_id': '目标ID',
    'target_name': '目标名称',
    'detail': '详情JSON',
    'value': '值(JSON)',
    'key': '键',
    'file_name': '文件名',
    'file_type': '文件类型',
    'products_count': '产品数量',
    'packages_count': '包数量',
    'accounts_count': '账户数量',
    'mcc_count': 'MCC数量',
    'videos_count': '视频数量',
    'copywritings_count': '文案数量',
    'tags_count': '标签数量',
    'skipped_count': '跳过数量',
    'change_type': '变更类型: manual/auto',
    'old_mcc_id': '旧MCC ID',
    'new_mcc_id': '新MCC ID',
    'changed_by': '操作人ID',
    'old_bm_id': '旧BM ID',
    'new_bm_id': '新BM ID',
    'login_email': '登录邮箱',
    'login_password': '登录密码',
    'backup_email': '备用邮箱',
    'backup_phone': '备用手机号',
    'consume_date': '消耗日期',
    'added_by': '添加者ID',
    'added_at': '添加时间',
    'line_name': '线名称',
    'platform_actual': '平台实际',
    'saved_at': '保存时间',
    'title': '标题',
}

def col_to_field(col):
    parts = col.split('_')
    return parts[0] + ''.join(p.capitalize() for p in parts[1:])

count = 0
files_count = 0
for root, dirs, files in os.walk(BASE):
    for f in files:
        if not f.endswith('.java') or f.endswith('Id.java'): continue
        path = os.path.join(root, f)
        with open(path, 'r', encoding='utf-8') as fp:
            lines = fp.readlines()

        updated = False
        new_lines = []
        for i, line in enumerate(lines):
            m = re.match(r'(\s*)private\s+(\S+)\s+(\w+)\s*;', line)
            if m:
                field = m.group(3)
                # 从字段名反推列名
                col = ''
                for ch in field:
                    if ch.isupper() and col:
                        col += '_'
                    col += ch.lower()

                comment = COL_CN.get(col) or COL_CN.get(field)
                if comment:
                    indent = m.group(1)
                    prev = lines[i-1].strip() if i > 0 else ''
                    if not prev.startswith('/**') and not prev.startswith('*'):
                        new_lines.append(f'{indent}/** {comment} */\n')
                        updated = True
                        count += 1
            new_lines.append(line)

        if updated:
            with open(path, 'w', encoding='utf-8') as fp:
                fp.writelines(new_lines)
            files_count += 1

print(f'{files_count} files, {count} field comments added')
PYEOF