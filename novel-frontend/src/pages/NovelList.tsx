// 小说列表页面：以卡片网格展示所有小说，支持创建、编辑、删除、按题材标签筛选和点击进入详情
import { useState, useEffect } from 'react';
import { Card, Button, Modal, Form, Input, Select, Tag, Empty, Spin, message, Popconfirm } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, BookOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { novelApi } from '../api/novel';
import type { Novel } from '../types';

const { Meta } = Card;
const { TextArea } = Input;

// 题材 → 标签颜色映射
const genreColors: Record<string, string> = {
  '玄幻': 'purple',
  '奇幻': 'magenta',
  '武侠': 'red',
  '仙侠': 'volcano',
  '都市': 'blue',
  '现实': 'cyan',
  '军事': 'green',
  '历史': 'gold',
  '游戏': 'lime',
  '体育': 'orange',
  '科幻': 'geekblue',
  '悬疑': '#f50',
  '轻小说': '#87d068',
};

export default function NovelList() {
  const navigate = useNavigate();
  const [novels, setNovels] = useState<Novel[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingNovel, setEditingNovel] = useState<Novel | null>(null);
  const [form] = Form.useForm();

  // 从后端加载所有小说列表
  const fetchNovels = async () => {
    setLoading(true);
    try {
      const response = await novelApi.getAll();
      setNovels(response.data.data || []);
    } catch (error) {
      message.error('获取小说列表失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNovels();
  }, []);

  // 打开新建小说弹窗
  const handleCreate = () => {
    setEditingNovel(null);
    form.resetFields();
    setModalOpen(true);
  };

  // 打开编辑小说弹窗，回填已有数据
  const handleEdit = (novel: Novel) => {
    setEditingNovel(novel);
    form.setFieldsValue({
      title: novel.title,
      description: novel.description,
      genre: novel.genre,
    });
    setModalOpen(true);
  };

  // 提交新建/编辑表单
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingNovel) {
        await novelApi.update(editingNovel.id, values);
        message.success('更新成功');
      } else {
        await novelApi.create(values);
        message.success('创建成功');
      }
      setModalOpen(false);
      fetchNovels();
    } catch (error: any) {
      if (error.response) {
        message.error(error.response.data?.message || '操作失败');
      }
    }
  };

  // 确认删除小说
  const handleDelete = async (id: number) => {
    try {
      await novelApi.delete(id);
      message.success('删除成功');
      fetchNovels();
    } catch (error) {
      message.error('删除失败');
    }
  };

  // 小说状态 → 中文显示文字及对应标签颜色
  const statusMap: Record<string, { text: string; color: string }> = {
    'draft': { text: '草稿', color: 'default' },
    'in_progress': { text: '创作中', color: 'processing' },
    'completed': { text: '已完成', color: 'success' },
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <h1 className="page-title">我的小说</h1>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
          新建小说
        </Button>
      </div>

      {loading ? (
        // 加载中状态
        <div style={{ textAlign: 'center', padding: '100px 0' }}>
          <Spin size="large" />
        </div>
      ) : novels.length === 0 ? (
        // 空状态提示
        <Empty
          description="还没有小说，点击上方按钮创建第一部"
          style={{ padding: '100px 0' }}
        />
      ) : (
        // 小说卡片网格：点击卡片进入详情，右下角提供编辑和删除操作
        <div className="card-grid">
          {novels.map((novel) => (
            <Card
              key={novel.id}
              hoverable
              onClick={() => navigate(`/novels/${novel.id}`)}
              actions={[
                <EditOutlined key="edit" onClick={(e) => { e.stopPropagation(); handleEdit(novel); }} />,
                <Popconfirm
                  key="delete"
                  title="确定删除这部小说吗？"
                  description="删除后无法恢复"
                  onConfirm={(e) => { e?.stopPropagation(); handleDelete(novel.id); }}
                  onCancel={(e) => e?.stopPropagation()}
                >
                  <DeleteOutlined onClick={(e) => e.stopPropagation()} />
                </Popconfirm>,
              ]}
            >
              <Meta
                avatar={<BookOutlined style={{ fontSize: '32px', color: '#1890ff' }} />}
                title={novel.title}
                description={
                  <div>
                    <div style={{ marginBottom: '8px' }}>
                      {novel.genre && (
                        <Tag color={genreColors[novel.genre] || 'default'}>
                          {novel.genre}
                        </Tag>
                      )}
                      <Tag color={statusMap[novel.status]?.color || 'default'}>
                        {statusMap[novel.status]?.text || novel.status}
                      </Tag>
                    </div>
                    <div style={{ color: '#666', fontSize: '13px', marginBottom: '8px' }}>
                      {novel.description || '暂无简介'}
                    </div>
                    <div style={{ color: '#999', fontSize: '12px' }}>
                      {novel.chapterCount} 章 · 更新于 {new Date(novel.updatedAt).toLocaleDateString()}
                    </div>
                  </div>
                }
              />
            </Card>
          ))}
        </div>
      )}

      {/* 新建/编辑小说弹窗 */}
      <Modal
        title={editingNovel ? '编辑小说' : '新建小说'}
        open={modalOpen}
        onOk={handleSubmit}
        onCancel={() => setModalOpen(false)}
        okText={editingNovel ? '保存' : '创建'}
        cancelText="取消"
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="title"
            label="小说标题"
            rules={[{ required: true, message: '请输入标题' }]}
          >
            <Input placeholder="请输入小说标题" />
          </Form.Item>
          <Form.Item name="genre" label="题材类型">
            <Select placeholder="选择题材类型" allowClear>
              {Object.keys(genreColors).map((genre) => (
                <Select.Option key={genre} value={genre}>
                  {genre}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="description" label="小说简介">
            <TextArea rows={4} placeholder="请输入小说简介" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
