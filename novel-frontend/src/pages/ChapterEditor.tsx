// 章节编辑器页面：提供富文本编辑/预览、AI 生成章节内容、AI 修订、保存及修订历史查看
import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Button, Space, Input, Tag, message, Spin, Card, Typography, Modal,
  Form, Select, Drawer, List, Divider,
} from 'antd';
import {
  ArrowLeftOutlined, SaveOutlined, RobotOutlined, EditOutlined,
  HistoryOutlined, EyeOutlined,
} from '@ant-design/icons';
import { chapterApi } from '../api/chapter';
import { aiApi } from '../api/ai';
import type { Chapter, Revision } from '../types';

const { TextArea } = Input;
const { Text, Paragraph } = Typography;

export default function ChapterEditor() {
  const { novelId, chapterId } = useParams<{ novelId: string; chapterId: string }>();
  const navigate = useNavigate();

  // 章节数据状态：完整章节对象、正文、标题、摘要
  const [chapter, setChapter] = useState<Chapter | null>(null);
  const [content, setContent] = useState('');
  const [title, setTitle] = useState('');
  const [summary, setSummary] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  // AI 弹窗状态
  const [aiModal, setAiModal] = useState(false);
  const [aiLoading, setAiLoading] = useState(false);
  const [aiForm] = Form.useForm();

  // 修订历史抽屉
  const [revisionDrawer, setRevisionDrawer] = useState(false);
  const [revisions] = useState<Revision[]>([]);

  // 预览模式切换
  const [previewMode, setPreviewMode] = useState(false);

  useEffect(() => {
    fetchChapter();
  }, [chapterId]);

  // 加载完整章节数据（含正文内容）
  const fetchChapter = async () => {
    setLoading(true);
    try {
      const response = await chapterApi.getById(Number(chapterId));
      const ch = response.data.data!;
      setChapter(ch);
      setContent(ch.content || '');
      setTitle(ch.title || '');
      setSummary(ch.summary || '');
    } catch (error) {
      message.error('加载章节失败');
    } finally {
      setLoading(false);
    }
  };

  // 保存章节：提交标题、正文、摘要，自动根据内容长度设置状态
  const handleSave = async () => {
    setSaving(true);
    try {
      await chapterApi.update(Number(chapterId), {
        title,
        content,
        summary,
        status: content.length > 0 ? 'writing' : 'draft',
      });
      message.success('保存成功');
    } catch (error) {
      message.error('保存失败');
    } finally {
      setSaving(false);
    }
  };

  // AI 生成章节正文：根据指令和字数要求调用后端 AI 接口
  const handleAiGenerate = async () => {
    setAiLoading(true);
    try {
      const values = await aiForm.validateFields();
      const response = await aiApi.generateChapter(Number(novelId), {
        chapterId: Number(chapterId),
        instructions: values.instructions,
        wordCount: values.wordCount || 3000,
      });
      const generated = response.data.data!;
      setContent(generated.content);
      message.success(`AI 生成完成，共 ${generated.wordCount} 字`);
      setAiModal(false);
      aiForm.resetFields();
    } catch (error: any) {
      message.error(error.response?.data?.message || 'AI 生成失败');
    } finally {
      setAiLoading(false);
    }
  };

  // AI 修订章节：支持全文修订或选中文本局部修订
  const handleAiRevise = async (selectedText?: string) => {
    setAiLoading(true);
    try {
      const values = await aiForm.validateFields();
      const response = await aiApi.reviseChapter(Number(chapterId), {
        selectedText,
        instructions: values.instructions,
        style: values.style || 'enhance',
      });
      const result = response.data.data!;
      if (selectedText) {
        setContent(content.replace(selectedText, result.revisedContent));
      } else {
        setContent(result.revisedContent);
      }
      message.success('AI 修订完成');
      setAiModal(false);
      aiForm.resetFields();
    } catch (error: any) {
      message.error(error.response?.data?.message || 'AI 修订失败');
    } finally {
      setAiLoading(false);
    }
  };

  // 打开修订历史抽屉（后端接口待对接）
  const loadRevisions = async () => {
    try {
      setRevisionDrawer(true);
    } catch (error) {
      message.error('加载修订历史失败');
    }
  };

  const wordCount = content.length;

  if (loading) {
    return <div style={{ textAlign: 'center', padding: '100px 0' }}><Spin size="large" /></div>;
  }

  if (!chapter) {
    return <div>章节不存在</div>;
  }

  return (
    <div style={{ padding: '0' }}>
      {/* 编辑器顶部工具栏：返回、标题、字数统计、状态标签、预览、修订历史、AI 辅助、保存 */}
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '16px',
        padding: '12px 0',
        borderBottom: '1px solid #f0f0f0',
      }}>
        <Space>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate(`/novels/${novelId}`)}>
            返回
          </Button>
          <Input
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="章节标题"
            style={{ width: 300, fontSize: '16px' }}
            variant="borderless"
          />
        </Space>
        <Space>
          <Text type="secondary">{wordCount} 字</Text>
          <Tag color={chapter.status === 'completed' ? 'success' : 'processing'}>
            {chapter.status === 'draft' ? '草稿' : chapter.status === 'writing' ? '写作中' : chapter.status === 'revised' ? '已修订' : '已完成'}
          </Tag>
          <Button icon={<EyeOutlined />} onClick={() => setPreviewMode(!previewMode)}>
            {previewMode ? '编辑' : '预览'}
          </Button>
          <Button icon={<HistoryOutlined />} onClick={loadRevisions}>
            修订历史
          </Button>
          <Button icon={<RobotOutlined />} onClick={() => { setAiModal(true); aiForm.resetFields(); }}>
            AI 辅助
          </Button>
          <Button type="primary" icon={<SaveOutlined />} onClick={handleSave} loading={saving}>
            保存
          </Button>
        </Space>
      </div>

      {/* 章节摘要区域：用于为 AI 提供上下文信息 */}
      <div style={{ marginBottom: '16px' }}>
        <TextArea
          value={summary}
          onChange={(e) => setSummary(e.target.value)}
          placeholder="章节摘要（用于 AI 上下文，可选）"
          autoSize={{ minRows: 1, maxRows: 3 }}
          style={{ fontSize: '13px' }}
        />
      </div>

      {/* 正文编辑区 / 预览模式切换 */}
      {previewMode ? (
        <Card style={{ minHeight: 500, whiteSpace: 'pre-wrap', lineHeight: 1.8, fontSize: '15px' }}>
          {content || '暂无内容'}
        </Card>
      ) : (
        <TextArea
          value={content}
          onChange={(e) => setContent(e.target.value)}
          placeholder="开始创作你的章节内容..."
          autoSize={{ minRows: 20 }}
          style={{
            fontSize: '15px',
            lineHeight: 1.8,
            fontFamily: "'Noto Serif SC', serif",
          }}
        />
      )}

      {/* AI 辅助创作弹窗：支持指令输入、字数设定、修订风格选择，提供生成和修订两个入口 */}
      <Modal
        title="AI 辅助创作"
        open={aiModal}
        onCancel={() => setAiModal(false)}
        footer={null}
        width={500}
      >
        <Form form={aiForm} layout="vertical">
          <Form.Item name="instructions" label="指令" rules={[{ required: true, message: '请输入指令' }]}>
            <TextArea rows={3} placeholder="描述你希望 AI 如何帮助你..." />
          </Form.Item>
          <Form.Item name="wordCount" label="目标字数" initialValue={3000}>
            <Input type="number" min={500} max={10000} step={500} />
          </Form.Item>
          <Form.Item name="style" label="修订风格" initialValue="enhance">
            <Select>
              <Select.Option value="enhance">增强描写</Select.Option>
              <Select.Option value="simplify">简化文字</Select.Option>
              <Select.Option value="formalize">正式化</Select.Option>
              <Select.Option value="emotional">增加情感</Select.Option>
            </Select>
          </Form.Item>
          <Space>
            <Button type="primary" icon={<RobotOutlined />} onClick={handleAiGenerate} loading={aiLoading}>
              生成章节内容
            </Button>
            <Button icon={<EditOutlined />} onClick={() => handleAiRevise()} loading={aiLoading}>
              修订当前内容
            </Button>
          </Space>
        </Form>
      </Modal>

      {/* 修订历史侧边抽屉：展示历次 AI 修订记录及反馈 */}
      <Drawer
        title="修订历史"
        placement="right"
        width={500}
        open={revisionDrawer}
        onClose={() => setRevisionDrawer(false)}
      >
        <List
          dataSource={revisions}
          locale={{ emptyText: '暂无修订记录' }}
          renderItem={(revision) => (
            <List.Item>
              <List.Item.Meta
                title={new Date(revision.createdAt).toLocaleString()}
                description={
                  <div>
                    <Text type="secondary">{revision.feedback}</Text>
                    <Divider />
                    <Paragraph ellipsis={{ rows: 3 }}>{revision.revisedContent}</Paragraph>
                  </div>
                }
              />
            </List.Item>
          )}
        />
      </Drawer>
    </div>
  );
}
