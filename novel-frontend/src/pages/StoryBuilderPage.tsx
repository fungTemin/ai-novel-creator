// StoryBuilder 对话式创作页面：通过分步骤聊天引导用户完成从故事梗概到审阅打磨的完整创作流程
import { useState, useRef, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Button, Input,   Card, Steps, Typography, Space, message, Spin,
  Popconfirm, Tag, Tooltip,
} from 'antd';
import {
  RobotOutlined, SendOutlined, ArrowLeftOutlined, ReloadOutlined,
  CheckCircleOutlined, FileTextOutlined, UserOutlined, GlobalOutlined,
  OrderedListOutlined, EditOutlined, AimOutlined, BookOutlined,
} from '@ant-design/icons';
import { storyBuilderApi } from '../api/storyBuilder';
import type { StoryBuilderState } from '../types';

const { TextArea } = Input;
const { Text, Title } = Typography;

// 创作流程的七个阶段配置
const stepConfig = [
  { key: 'story_premise', label: '故事梗概', icon: <BookOutlined /> },
  { key: 'genre_style', label: '题材风格', icon: <AimOutlined /> },
  { key: 'character_concept', label: '角色构思', icon: <UserOutlined /> },
  { key: 'world_concept', label: '世界观', icon: <GlobalOutlined /> },
  { key: 'plot_outline', label: '情节大纲', icon: <OrderedListOutlined /> },
  { key: 'chapter_drafting', label: '逐章创作', icon: <EditOutlined /> },
  { key: 'review_polish', label: '审阅打磨', icon: <CheckCircleOutlined /> },
];

// 步骤 key → 中文标签的快速查找映射
const stepLabels: Record<string, string> = Object.fromEntries(
  stepConfig.map(s => [s.key, s.label])
);

export default function StoryBuilderPage() {
  const { id } = useParams<{ id: string }>();
  const novelId = Number(id);
  const navigate = useNavigate();

  // StoryBuilder 状态：完整状态对象、对话消息列表、输入文本、加载状态
  const [state, setState] = useState<StoryBuilderState | null>(null);
  const [messages, setMessages] = useState<Array<{ role: string; content: string; options?: string[] }>>([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [initializing, setInitializing] = useState(true);
  const [currentStep, setCurrentStep] = useState(0);

  // 用于自动滚动到聊天底部
  const chatEndRef = useRef<HTMLDivElement>(null);

  // 新消息到达时自动滚动到底部
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // 页面加载时初始化 builder
  useEffect(() => {
    initBuilder();
  }, [novelId]);

  // 启动创作助手：调用 start 接口，初始化对话状态
  const initBuilder = async () => {
    setInitializing(true);
    try {
      const res = await storyBuilderApi.start(novelId);
      const data = res.data.data!;
      setState(data.state);
      setMessages(data.state.chatHistory || []);
      setCurrentStep(data.state.stepIndex - 1);
    } catch (error: any) {
      message.error('启动创作助手失败');
    } finally {
      setInitializing(false);
    }
  };

  // 发送用户消息：先乐观渲染用户消息，再调用 chat 接口获取 AI 回复
  const handleSend = async (text?: string) => {
    const msg = text || input;
    if (!msg.trim()) return;

    const userMsg = { role: 'user', content: msg };
    setMessages(prev => [...prev, userMsg]);
    setInput('');
    setLoading(true);

    try {
      const res = await storyBuilderApi.chat(novelId, { novelId, message: msg, action: 'continue' });
      const data = res.data.data!;
      setState(data.state);
      setMessages(data.state.chatHistory || []);
      setCurrentStep(data.state.stepIndex - 1);
    } catch (error: any) {
      message.error('发送失败');
    } finally {
      setLoading(false);
    }
  };

  // 用户点击快捷选项时触发
  const handleOption = (option: string) => {
    handleSend(option);
  };

  // 返回上一步对话
  const handleBack = async () => {
    setLoading(true);
    try {
      const res = await storyBuilderApi.chat(novelId, { novelId, message: '', action: 'back' });
      const data = res.data.data!;
      setState(data.state);
      setMessages(data.state.chatHistory || []);
      setCurrentStep(data.state.stepIndex - 1);
    } catch (error: any) {
      message.error('返回失败');
    } finally {
      setLoading(false);
    }
  };

  // 重新开始整个创作流程
  const handleRestart = async () => {
    setLoading(true);
    try {
      const res = await storyBuilderApi.chat(novelId, { novelId, message: '', action: 'restart' });
      const data = res.data.data!;
      setState(data.state);
      setMessages(data.state.chatHistory || []);
      setCurrentStep(data.state.stepIndex - 1);
      message.success('已重新开始');
    } catch (error: any) {
      message.error('重启失败');
    } finally {
      setLoading(false);
    }
  };

  // 点击步骤条切换创作阶段
  const handleStepClick = async (stepIndex: number) => {
    const step = stepConfig[stepIndex];
    if (!step) return;
    setLoading(true);
    try {
      const res = await storyBuilderApi.startStep(novelId, { novelId, step: step.key, action: 'start' });
      const data = res.data.data!;
      setState(data.state);
      setMessages(data.state.chatHistory || []);
      setCurrentStep(data.state.stepIndex - 1);
    } catch (error: any) {
      message.error('切换步骤失败');
    } finally {
      setLoading(false);
    }
  };

  // 渲染单条对话气泡：根据角色显示不同样式，支持 Markdown 转 HTML 和快捷选项按钮
  const renderMessage = (msg: { role: string; content: string; options?: string[] }, idx: number) => {
    const isUser = msg.role === 'user';
    const isSystem = msg.role === 'system';

    let icon = <RobotOutlined style={{ color: '#1677ff', fontSize: 20 }} />;
    if (isUser) icon = <UserOutlined style={{ color: '#52c41a', fontSize: 20 }} />;
    if (isSystem) icon = <CheckCircleOutlined style={{ color: '#faad14', fontSize: 20 }} />;

    // AI 消息显示当前步骤标签
    const stepHeader = !isUser && !isSystem ? stepLabels[currentStep] : null;

    // 将类 Markdown 格式转换为 HTML 以增强显示
    const formattedContent = msg.content
      .replace(/^##\s+(.*)$/gm, (_, title) => `<h4 style="margin:12px 0 8px;color:#1677ff">${title}</h4>`)
      .replace(/^###\s+(.*)$/gm, (_, title) => `<p style="margin:8px 0 4px;font-weight:600">${title}</p>`)
      .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
      .replace(/\n- /g, '<br/>• ')
      .replace(/\n/g, '<br/>');

    return (
      <div key={idx} style={{
        display: 'flex',
        gap: 12,
        marginBottom: 16,
        flexDirection: isUser ? 'row-reverse' : 'row',
      }}>
        <div style={{
          width: 36, height: 36, borderRadius: '50%',
          background: isUser ? '#f6ffed' : '#f0f5ff',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          flexShrink: 0,
        }}>
          {icon}
        </div>
        <div style={{
          maxWidth: '80%',
          background: isUser ? '#f6ffed' : '#fafafa',
          borderRadius: 12,
          padding: '12px 16px',
          border: '1px solid',
          borderColor: isUser ? '#b7eb8f' : '#e8e8e8',
        }}>
          {stepHeader && (
            <Tag color="blue" style={{ marginBottom: 8 }}>{stepHeader}</Tag>
          )}
          <div
            style={{ lineHeight: 1.7, fontSize: 14 }}
            dangerouslySetInnerHTML={{ __html: formattedContent }}
          />
          {msg.options && msg.options.length > 0 && (
            <div style={{ marginTop: 12, display: 'flex', flexWrap: 'wrap', gap: 6 }}>
              {msg.options.map((opt, oi) => (
                <Button
                  key={oi}
                  size="small"
                  type="dashed"
                  onClick={() => handleOption(opt)}
                  disabled={loading}
                >
                  {opt}
                </Button>
              ))}
            </div>
          )}
        </div>
      </div>
    );
  };

  if (initializing) {
    return <div style={{ textAlign: 'center', padding: '100px 0' }}><Spin size="large" tip="启动创作助手..." /></div>;
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: 'calc(100vh - 140px)', maxWidth: 900, margin: '0 auto' }}>
      {/* 顶部标题栏：返回按钮、标题、重新开始 */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <Space>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate(`/novels/${novelId}`)}>
            返回
          </Button>
          <Title level={4} style={{ margin: 0 }}>
            <RobotOutlined style={{ marginRight: 8 }} />
            AI 创作助手
          </Title>
        </Space>
        <Space>
          <Popconfirm title="确定重新开始？当前对话记录将丢失。" onConfirm={handleRestart}>
            <Button icon={<ReloadOutlined />} disabled={loading}>重新开始</Button>
          </Popconfirm>
        </Space>
      </div>

      {/* 步骤进度条：展示创作流程的七个阶段，可点击跳转 */}
      <Card size="small" style={{ marginBottom: 16 }}>
        <Steps
          current={currentStep}
          size="small"
          onChange={handleStepClick}
          items={stepConfig.map((s, i) => ({
            title: s.label,
            icon: s.icon,
            status: i < currentStep ? 'finish' : i === currentStep ? 'process' : 'wait',
          }))}
          style={{ cursor: 'pointer' }}
        />
      </Card>

      {/* 对话消息区：滚动容器，按角色渲染气泡，底部自动定位 */}
      <Card
        style={{
          flex: 1,
          overflowY: 'auto',
          marginBottom: 12,
          background: '#fff',
        }}
        bodyStyle={{ padding: '16px 20px' }}
      >
        {messages.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '60px 0', color: '#999' }}>
            <RobotOutlined style={{ fontSize: 48, marginBottom: 16 }} />
            <p>正在准备创作助手...</p>
          </div>
        ) : (
          messages.map((msg, idx) => renderMessage(msg, idx))
        )}
        {loading && (
          <div style={{ display: 'flex', gap: 12, alignItems: 'center', padding: 8 }}>
            <Spin size="small" />
            <Text type="secondary" style={{ fontSize: 13 }}>AI 思考中...</Text>
          </div>
        )}
        <div ref={chatEndRef} />
      </Card>

      {/* 底部输入区：文本框+发送按钮，支持 Shift+Enter 换行、Enter 发送 */}
      <Card size="small">
        <div style={{ display: 'flex', gap: 8 }}>
          <TextArea
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="输入你的想法..."
            autoSize={{ minRows: 1, maxRows: 4 }}
            onPressEnter={(e) => {
              if (!e.shiftKey) {
                e.preventDefault();
                handleSend();
              }
            }}
            disabled={loading}
          />
          <Button
            type="primary"
            icon={<SendOutlined />}
            onClick={() => handleSend()}
            loading={loading}
            style={{ height: 'auto', minHeight: 36 }}
          >
            发送
          </Button>
        </div>
        <div style={{ marginTop: 8, display: 'flex', justifyContent: 'space-between' }}>
          <Space>
            <Button size="small" onClick={handleBack} disabled={loading || currentStep <= 0}>
              ← 上一步
            </Button>
            {state && (
              <Text type="secondary" style={{ fontSize: 12 }}>
                步骤 {state.stepIndex} / {state.totalSteps} · 已完成 {state.completedSteps.length} 步
              </Text>
            )}
          </Space>
          <Space>
            {state?.currentStep === 'review_polish' && (
              <Tooltip title="查看小说详情">
                <Button size="small" icon={<FileTextOutlined />} onClick={() => navigate(`/novels/${novelId}`)}>
                  返回小说
                </Button>
              </Tooltip>
            )}
          </Space>
        </div>
      </Card>
    </div>
  );
}
