// 小说仪表盘页面：以标签页形式集中管理章节、角色、大纲和世界观，集成 AI 生成和审查功能
import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Tabs, Button, List, Tag, Space, Modal, Form, Input, InputNumber,
  message, Popconfirm, Empty, Spin, Descriptions, Typography, Alert, Divider, Collapse,
} from 'antd';
import {
  PlusOutlined, EditOutlined, DeleteOutlined, RobotOutlined,
  FileTextOutlined, UserOutlined, GlobalOutlined, OrderedListOutlined,
  ApartmentOutlined, BranchesOutlined, CheckCircleOutlined,
  ExpandOutlined, ExperimentOutlined,
} from '@ant-design/icons';
import { novelApi } from '../api/novel';
import { chapterApi } from '../api/chapter';
import { characterApi } from '../api/character';
import { worldBuildingApi, plotApi, styleApi, aiApi } from '../api/ai';
import type { Novel, ChapterBrief, Character, PlotOutline, WorldBuilding, StyleSetting, PlotBranch, CharacterArcSuggestion, WorldConsistencyIssue, PlotReviewResult, SuggestedCategory } from '../types';

const { TextArea } = Input;
const { Text, Title } = Typography;

export default function NovelDashboard() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const novelId = Number(id);

  // 核心数据状态：小说、章节、角色、大纲、世界观、风格设定
  const [novel, setNovel] = useState<Novel | null>(null);
  const [chapters, setChapters] = useState<ChapterBrief[]>([]);
  const [characters, setCharacters] = useState<Character[]>([]);
  const [outlines, setOutlines] = useState<PlotOutline[]>([]);
  const [worldBuilding, setWorldBuilding] = useState<WorldBuilding[]>([]);
  const [, setStyleSetting] = useState<StyleSetting | null>(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('chapters');

  // 基础管理弹窗状态
  const [chapterModal, setChapterModal] = useState(false);
  const [characterModal, setCharacterModal] = useState(false);
  const [outlineModal, setOutlineModal] = useState(false);
  const [worldModal, setWorldModal] = useState(false);
  const [aiModal, setAiModal] = useState<string | null>(null);
  const [aiLoading, setAiLoading] = useState(false);

  // 基础表单实例
  const [chapterForm] = Form.useForm();
  const [characterForm] = Form.useForm();
  const [outlineForm] = Form.useForm();
  const [worldForm] = Form.useForm();
  const [aiForm] = Form.useForm();

  // 世界观增强功能弹窗状态
  const [worldBatchModal, setWorldBatchModal] = useState(false);
  const [suggestCategoryModal, setSuggestCategoryModal] = useState(false);
  const [categoryResults, setCategoryResults] = useState<SuggestedCategory[]>([]);
  const [categoryLoading, setCategoryLoading] = useState(false);
  const [consistencyModal, setConsistencyModal] = useState(false);
  const [consistencyResults, setConsistencyResults] = useState<WorldConsistencyIssue[]>([]);
  const [consistencyLoading, setConsistencyLoading] = useState(false);
  const [expandWorldModal, setExpandWorldModal] = useState(false);
  const [expandWorldId, setExpandWorldId] = useState<number>(0);
  const [expandWorldLoading, setExpandWorldLoading] = useState(false);

  // 大纲增强功能弹窗状态
  const [reviewPlotModal, setReviewPlotModal] = useState(false);
  const [plotReviewResult, setPlotReviewResult] = useState<PlotReviewResult | null>(null);
  const [reviewLoading, setReviewLoading] = useState(false);
  const [expandPlotModal, setExpandPlotModal] = useState(false);
  const [expandPlotLoading, setExpandPlotLoading] = useState(false);
  const [branchModal, setBranchModal] = useState(false);
  const [branches, setBranches] = useState<PlotBranch[]>([]);
  const [branchLoading, setBranchLoading] = useState(false);
  const [charArcModal, setCharArcModal] = useState(false);
  const [charArcResults, setCharArcResults] = useState<CharacterArcSuggestion[]>([]);
  const [charArcLoading, setCharArcLoading] = useState(false);
  const [foreshadowModal, setForeshadowModal] = useState(false);
  const [foreshadowResults, setForeshadowResults] = useState<ForeshadowingReviewResult | null>(null);
  const [foreshadowLoading, setForeshadowLoading] = useState(false);

  // 增强功能表单实例
  const [worldBatchForm] = Form.useForm();
  const [suggestForm] = Form.useForm();
  const [expandForm] = Form.useForm();
  const [reviewForm] = Form.useForm();
  const [expandPlotForm] = Form.useForm();
  const [branchForm] = Form.useForm();
  const [charArcForm] = Form.useForm();

  // 页面加载时获取所有相关数据
  useEffect(() => {
    fetchData();
  }, [novelId]);

  // 并行加载小说及其所有关联数据（章节、角色、大纲、世界观、风格）
  const fetchData = async () => {
    setLoading(true);
    try {
      const [novelRes, chaptersRes, charactersRes, outlinesRes, worldRes, styleRes] = await Promise.all([
        novelApi.getById(novelId),
        chapterApi.getAll(novelId),
        characterApi.getAll(novelId),
        plotApi.getAll(novelId),
        worldBuildingApi.getAll(novelId),
        styleApi.get(novelId).catch(() => ({ data: { data: null } })),
      ]);
      setNovel(novelRes.data.data!);
      setChapters(chaptersRes.data.data || []);
      setCharacters(charactersRes.data.data || []);
      setOutlines(outlinesRes.data.data || []);
      setWorldBuilding(worldRes.data.data || []);
      setStyleSetting(styleRes.data.data);
    } catch (error) {
      message.error('加载数据失败');
    } finally {
      setLoading(false);
    }
  };

  // ========= 章节管理操作 =========

  // 添加新章节：自动计算下一章序号
  const handleAddChapter = async () => {
    try {
      const values = await chapterForm.validateFields();
      const nextNumber = chapters.length > 0
        ? Math.max(...chapters.map(c => c.chapterNumber)) + 1
        : 1;
      await chapterApi.create(novelId, { ...values, chapterNumber: values.chapterNumber || nextNumber });
      message.success('章节创建成功');
      setChapterModal(false);
      chapterForm.resetFields();
      fetchData();
    } catch (error: any) {
      if (error.response) message.error('创建失败');
    }
  };

  // 删除指定章节
  const handleDeleteChapter = async (chapterId: number) => {
    try {
      await chapterApi.delete(chapterId);
      message.success('删除成功');
      fetchData();
    } catch (error) {
      message.error('删除失败');
    }
  };

  // ========= 角色管理操作 =========

  // 添加新角色
  const handleAddCharacter = async () => {
    try {
      const values = await characterForm.validateFields();
      await characterApi.create(novelId, values);
      message.success('角色创建成功');
      setCharacterModal(false);
      characterForm.resetFields();
      fetchData();
    } catch (error: any) {
      if (error.response) message.error('创建失败');
    }
  };

  // 删除指定角色
  const handleDeleteCharacter = async (characterId: number) => {
    try {
      await characterApi.delete(characterId);
      message.success('删除成功');
      fetchData();
    } catch (error) {
      message.error('删除失败');
    }
  };

  // ========= 大纲管理操作 =========

  // 添加新大纲节点
  const handleAddOutline = async () => {
    try {
      const values = await outlineForm.validateFields();
      await plotApi.create(novelId, { ...values, orderIndex: outlines.length });
      message.success('大纲创建成功');
      setOutlineModal(false);
      outlineForm.resetFields();
      fetchData();
    } catch (error: any) {
      if (error.response) message.error('创建失败');
    }
  };

  // 删除指定大纲节点
  const handleDeleteOutline = async (outlineId: number) => {
    try {
      await plotApi.delete(outlineId);
      message.success('删除成功');
      fetchData();
    } catch (error) {
      message.error('删除失败');
    }
  };

  // ========= 世界观管理操作 =========

  // 添加新世界观设定
  const handleAddWorld = async () => {
    try {
      const values = await worldForm.validateFields();
      await worldBuildingApi.create(novelId, values);
      message.success('世界观创建成功');
      setWorldModal(false);
      worldForm.resetFields();
      fetchData();
    } catch (error: any) {
      if (error.response) message.error('创建失败');
    }
  };

  // 删除指定世界观条目
  const handleDeleteWorld = async (worldId: number) => {
    try {
      await worldBuildingApi.delete(worldId);
      message.success('删除成功');
      fetchData();
    } catch (error) {
      message.error('删除失败');
    }
  };

  // ========= AI 生成操作（大纲/角色/世界观） =========

  // 根据类型调用对应的 AI 生成接口
  const handleAiGenerate = async (type: string) => {
    setAiLoading(true);
    try {
      const values = await aiForm.validateFields();
      if (type === 'plot') {
        await aiApi.generatePlot(novelId, values);
        message.success('大纲生成成功');
      } else if (type === 'character') {
        await aiApi.generateCharacter(novelId, values);
        message.success('角色生成成功');
      } else if (type === 'world') {
        await aiApi.generateWorld(novelId, values);
        message.success('世界观生成成功');
      }
      setAiModal(null);
      aiForm.resetFields();
      fetchData();
    } catch (error: any) {
      message.error(error.response?.data?.message || 'AI 生成失败');
    } finally {
      setAiLoading(false);
    }
  };

  // ========= 世界观增强功能 =========

  // AI 批量生成世界观：一次请求覆盖多个分类
  const handleWorldBatchGenerate = async () => {
    setAiLoading(true);
    try {
      const values = await worldBatchForm.validateFields();
      const cats = values.categories.split(',').map((s: string) => s.trim()).filter(Boolean);
      await aiApi.generateWorldBatch(novelId, {
        categories: cats,
        prompt: values.prompt,
        countPerCategory: values.countPerCategory || 3,
      });
      message.success('批量生成成功');
      setWorldBatchModal(false);
      worldBatchForm.resetFields();
      fetchData();
    } catch (error: any) {
      message.error(error.response?.data?.message || '批量生成失败');
    } finally {
      setAiLoading(false);
    }
  };

  // AI 建议世界观分类：基于题材和已有分类推荐新方向
  const handleSuggestCategories = async () => {
    setCategoryLoading(true);
    try {
      const values = await suggestForm.validateFields();
      const existingCats = [...new Set(worldBuilding.map(w => w.category))];
      const res = await aiApi.suggestWorldCategories(novelId, {
        genre: values.genre || novel?.genre || '',
        existingCategories: existingCats,
      });
      setCategoryResults(res.data.data || []);
      setSuggestCategoryModal(true);
    } catch (error: any) {
      message.error(error.response?.data?.message || '获取建议失败');
    } finally {
      setCategoryLoading(false);
    }
  };

  // AI 世界观一致性检查：检测不同设定条目间的矛盾
  const handleReviewConsistency = async () => {
    setConsistencyLoading(true);
    try {
      const res = await aiApi.reviewWorldConsistency(novelId, { worldIds: [] });
      setConsistencyResults(res.data.data || []);
      setConsistencyModal(true);
    } catch (error: any) {
      message.error(error.response?.data?.message || '一致性检查失败');
    } finally {
      setConsistencyLoading(false);
    }
  };

  // AI 扩展世界观细节：对已有设定从指定方面进行深度扩展
  const handleExpandWorld = async () => {
    setExpandWorldLoading(true);
    try {
      const values = await expandForm.validateFields();
      const res = await aiApi.expandWorldDetail(novelId, expandWorldId, {
        worldId: expandWorldId,
        aspect: values.aspect,
        detailLevel: values.detailLevel || 'detailed',
      });
      setExpandWorldModal(false);
      expandForm.resetFields();
      if (res.data.data) message.success(`「${res.data.data.name}」扩展成功`);
      fetchData();
    } catch (error: any) {
      message.error(error.response?.data?.message || '扩展失败');
    } finally {
      setExpandWorldLoading(false);
    }
  };

  // ========= 大纲增强功能 =========

  // AI 审查故事大纲：分析一致性、节奏、完整性等维度
  const handleReviewPlot = async () => {
    setReviewLoading(true);
    try {
      const values = await reviewForm.validateFields();
      const res = await aiApi.reviewPlot(novelId, { focus: values.focus || 'consistency' });
      setPlotReviewResult(res.data.data);
      setReviewPlotModal(true);
    } catch (error: any) {
      message.error(error.response?.data?.message || '审查失败');
    } finally {
      setReviewLoading(false);
    }
  };

  // AI 扩展情节：在大纲基础上细化子情节或补充新节点
  const handleExpandPlot = async () => {
    setExpandPlotLoading(true);
    try {
      const values = await expandPlotForm.validateFields();
      await aiApi.expandPlot(novelId, {
        outlineId: values.outlineId || undefined,
        focus: values.focus || 'subplot',
        depth: values.depth || 1,
      });
      setExpandPlotModal(false);
      expandPlotForm.resetFields();
      message.success('情节扩展成功');
      fetchData();
    } catch (error: any) {
      message.error(error.response?.data?.message || '扩展失败');
    } finally {
      setExpandPlotLoading(false);
    }
  };

  // AI 建议情节分支：围绕关键节点提出多个可选发展方向
  const handleSuggestBranches = async () => {
    setBranchLoading(true);
    try {
      const values = await branchForm.validateFields();
      const res = await aiApi.suggestPlotBranches(novelId, {
        outlineId: values.outlineId || undefined,
        keyPoint: values.keyPoint || '',
        branchCount: values.branchCount || 3,
      });
      setBranches(res.data.data || []);
      setBranchModal(true);
    } catch (error: any) {
      message.error(error.response?.data?.message || '获取分支建议失败');
    } finally {
      setBranchLoading(false);
    }
  };

  // AI 角色弧线整合分析：关联大纲节点与角色成长轨迹
  const handleIntegrateCharacterArc = async () => {
    setCharArcLoading(true);
    try {
      const values = await charArcForm.validateFields();
      const res = await aiApi.integrateCharacterArc(novelId, {
        outlineIds: [],
        characterId: values.characterId || undefined,
      });
      setCharArcResults(res.data.data || []);
      setCharArcModal(true);
    } catch (error: any) {
      message.error(error.response?.data?.message || '角色弧线整合分析失败');
    } finally {
      setCharArcLoading(false);
    }
  };

  // AI 伏笔审查：分析伏笔埋设与回收的完整性
  const handleReviewForeshadowing = async () => {
    setForeshadowLoading(true);
    try {
      const res = await aiApi.reviewForeshadowing(novelId, { outlineIds: [] });
      const data = res.data.data;
      if (data) setForeshadowResults(data);
      setForeshadowModal(true);
    } catch (error: any) {
      message.error(error.response?.data?.message || '伏笔分析失败');
    } finally {
      setForeshadowLoading(false);
    }
  };

  if (loading) {
    return <div style={{ textAlign: 'center', padding: '100px 0' }}><Spin size="large" /></div>;
  }

  if (!novel) {
    return <Empty description="小说不存在" />;
  }

  // 角色类型英文 → 中文映射
  const roleMap: Record<string, string> = {
    'protagonist': '主角',
    'antagonist': '反派',
    'supporting': '配角',
  };

  // 世界观分类英文 → 中文映射
  const categoryMap: Record<string, string> = {
    'geography': '地理',
    'culture': '文化',
    'magic_system': '力量体系',
    'history': '历史',
    'species': '种族',
  };

  // 严重程度 → Ant Design 标签颜色
  const severityColors: Record<string, string> = {
    'high': 'red',
    'medium': 'orange',
    'low': 'blue',
  };

  // 四个主要标签页配置：章节管理、角色管理、故事大纲、世界观
  const tabItems = [
    {
      key: 'chapters',
      label: <span><FileTextOutlined /> 章节管理</span>,
      children: (
        <div>
          <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
            <Text type="secondary">共 {chapters.length} 章</Text>
            <Space>
              <Button icon={<RobotOutlined />} onClick={() => { setAiModal('chapter'); aiForm.resetFields(); }}>
                AI 生成章节
              </Button>
              <Button type="primary" icon={<PlusOutlined />} onClick={() => { setChapterModal(true); chapterForm.resetFields(); }}>
                添加章节
              </Button>
            </Space>
          </div>
          <List
            dataSource={chapters}
            locale={{ emptyText: <Empty description="暂无章节" /> }}
            renderItem={(chapter) => (
              <List.Item
                actions={[
                  <Button type="link" icon={<EditOutlined />} onClick={() => navigate(`/novels/${novelId}/chapters/${chapter.id}`)}>
                    编辑
                  </Button>,
                  <Popconfirm title="确定删除？" onConfirm={() => handleDeleteChapter(chapter.id)}>
                    <Button type="link" danger icon={<DeleteOutlined />}>删除</Button>
                  </Popconfirm>,
                ]}
              >
                <List.Item.Meta
                  title={`第${chapter.chapterNumber}章 ${chapter.title || '未命名'}`}
                  description={
                    <Space>
                      <Tag>{chapter.status}</Tag>
                      <Text type="secondary">{chapter.wordCount} 字</Text>
                      {chapter.summary && <Text type="secondary" ellipsis style={{ maxWidth: 300 }}>{chapter.summary}</Text>}
                    </Space>
                  }
                />
              </List.Item>
            )}
          />
        </div>
      ),
    },
    // 角色管理标签：列出所有角色，支持 AI 生成和手动添加
    {
      key: 'characters',
      label: <span><UserOutlined /> 角色管理</span>,
      children: (
        <div>
          <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
            <Text type="secondary">共 {characters.length} 个角色</Text>
            <Space>
              <Button icon={<RobotOutlined />} onClick={() => { setAiModal('character'); aiForm.resetFields(); }}>
                AI 生成角色
              </Button>
              <Button type="primary" icon={<PlusOutlined />} onClick={() => { setCharacterModal(true); characterForm.resetFields(); }}>
                添加角色
              </Button>
            </Space>
          </div>
          <List
            dataSource={characters}
            locale={{ emptyText: <Empty description="暂无角色" /> }}
            renderItem={(character) => (
              <List.Item
                actions={[
                  <Popconfirm title="确定删除？" onConfirm={() => handleDeleteCharacter(character.id)}>
                    <Button type="link" danger icon={<DeleteOutlined />}>删除</Button>
                  </Popconfirm>,
                ]}
              >
                <List.Item.Meta
                  title={
                    <Space>
                      {character.name}
                      {character.role && <Tag color="blue">{roleMap[character.role] || character.role}</Tag>}
                    </Space>
                  }
                  description={
                    <div>
                      {character.description && <div>{character.description}</div>}
                      {character.personality && <div><Text type="secondary">性格: </Text>{character.personality}</div>}
                    </div>
                  }
                />
              </List.Item>
            )}
          />
        </div>
      ),
    },
    // 故事大纲标签：使用折叠面板展示，集成审查、扩展、分支、角色弧线、伏笔等 AI 工具
    {
      key: 'outlines',
      label: <span><OrderedListOutlined /> 故事大纲</span>,
      children: (
        <div>
          <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
            <Text type="secondary">共 {outlines.length} 条大纲</Text>
            <Space wrap>
              <Button icon={<RobotOutlined />} onClick={() => { setAiModal('plot'); aiForm.resetFields(); }}>
                生成大纲
              </Button>
              <Button icon={<CheckCircleOutlined />} onClick={handleReviewPlot} loading={reviewLoading}>
                审查大纲
              </Button>
              <Button icon={<ExpandOutlined />} onClick={() => { setExpandPlotModal(true); expandPlotForm.resetFields(); }}>
                扩展情节
              </Button>
              <Button icon={<BranchesOutlined />} onClick={() => { setBranchModal(true); branchForm.resetFields(); }}>
                分支建议
              </Button>
              <Button icon={<ApartmentOutlined />} onClick={handleIntegrateCharacterArc} loading={charArcLoading}>
                角色弧线
              </Button>
              <Button icon={<ExperimentOutlined />} onClick={handleReviewForeshadowing} loading={foreshadowLoading}>
                伏笔分析
              </Button>
              <Button type="primary" icon={<PlusOutlined />} onClick={() => { setOutlineModal(true); outlineForm.resetFields(); }}>
                手动添加
              </Button>
            </Space>
          </div>
          <Divider />
          <Collapse items={outlines.map((outline) => ({
            key: outline.id,
            label: <Text strong>{outline.title}</Text>,
            extra: (
              <Popconfirm title="确定删除？" onConfirm={() => handleDeleteOutline(outline.id)}>
                <Button type="link" danger icon={<DeleteOutlined />} size="small" />
              </Popconfirm>
            ),
            children: <Text type="secondary">{outline.summary || '暂无摘要'}</Text>,
          }))} />
        </div>
      ),
    },
    // 世界观标签：展示所有设定，支持 AI 生成、批量生成、分类建议、一致性检查、扩展细节
    {
      key: 'world',
      label: <span><GlobalOutlined /> 世界观</span>,
      children: (
        <div>
          <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
            <Text type="secondary">共 {worldBuilding.length} 条设定</Text>
            <Space wrap>
              <Button icon={<RobotOutlined />} onClick={() => { setAiModal('world'); aiForm.resetFields(); }}>
                生成设定
              </Button>
              <Button icon={<BranchesOutlined />} onClick={() => { setWorldBatchModal(true); worldBatchForm.resetFields(); }}>
                批量生成
              </Button>
              <Button icon={<ExperimentOutlined />} onClick={handleSuggestCategories} loading={categoryLoading}>
                建议分类
              </Button>
              <Button icon={<CheckCircleOutlined />} onClick={handleReviewConsistency} loading={consistencyLoading}>
                一致性检查
              </Button>
              <Button type="primary" icon={<PlusOutlined />} onClick={() => { setWorldModal(true); worldForm.resetFields(); }}>
                手动添加
              </Button>
            </Space>
          </div>
          <Divider />
          <List
            dataSource={worldBuilding}
            locale={{ emptyText: <Empty description="暂无世界观设定" /> }}
            renderItem={(world) => (
              <List.Item
                actions={[
                  <Button type="link" icon={<ExpandOutlined />} onClick={() => {
                    setExpandWorldId(world.id);
                    setExpandWorldModal(true);
                    expandForm.resetFields();
                  }}>
                    扩展细节
                  </Button>,
                  <Popconfirm title="确定删除？" onConfirm={() => handleDeleteWorld(world.id)}>
                    <Button type="link" danger icon={<DeleteOutlined />}>删除</Button>
                  </Popconfirm>,
                ]}
              >
                <List.Item.Meta
                  title={
                    <Space>
                      {world.name}
                      <Tag color="green">{categoryMap[world.category] || world.category}</Tag>
                    </Space>
                  }
                  description={world.description}
                />
              </List.Item>
            )}
          />
        </div>
      ),
    },
  ];

  return (
    <div>
      {/* 小说头部信息区：标题、状态标签、类型、简介及进入 AI 创作助手的入口 */}
      <Descriptions
        title={novel.title}
        extra={
          <Space>
            <Tag color={novel.status === 'completed' ? 'success' : novel.status === 'in_progress' ? 'processing' : 'default'}>
              {novel.status === 'draft' ? '草稿' : novel.status === 'in_progress' ? '创作中' : '已完成'}
            </Tag>
            <Button type="primary" icon={<RobotOutlined />} onClick={() => navigate(`/novels/${novel.id}/builder`)}>
              AI 创作助手
            </Button>
          </Space>
        }
        style={{ marginBottom: 24 }}
      >
        {novel.genre && <Descriptions.Item label="类型"><Tag>{novel.genre}</Tag></Descriptions.Item>}
        {novel.description && <Descriptions.Item label="简介">{novel.description}</Descriptions.Item>}
      </Descriptions>

      <Tabs activeKey={activeTab} onChange={setActiveTab} items={tabItems} />

      {/* 添加章节弹窗：填写标题和摘要即可创建新章节 */}
      <Modal title="添加章节" open={chapterModal} onOk={handleAddChapter} onCancel={() => setChapterModal(false)}>
        <Form form={chapterForm} layout="vertical">
          <Form.Item name="title" label="章节标题"><Input placeholder="请输入章节标题" /></Form.Item>
          <Form.Item name="summary" label="章节摘要"><TextArea rows={3} placeholder="请输入章节摘要（可选）" /></Form.Item>
        </Form>
      </Modal>

      {/* 添加角色弹窗：填写名称类型、外貌、性格和背景 */}
      <Modal title="添加角色" open={characterModal} onOk={handleAddCharacter} onCancel={() => setCharacterModal(false)} width={600}>
        <Form form={characterForm} layout="vertical">
          <Form.Item name="name" label="角色名" rules={[{ required: true, message: '请输入角色名' }]}><Input placeholder="请输入角色名" /></Form.Item>
          <Form.Item name="role" label="角色类型"><Input placeholder="protagonist / antagonist / supporting" /></Form.Item>
          <Form.Item name="description" label="外貌描述"><TextArea rows={2} placeholder="角色外貌描述" /></Form.Item>
          <Form.Item name="personality" label="性格特征"><TextArea rows={2} placeholder="角色性格特征" /></Form.Item>
          <Form.Item name="background" label="背景故事"><TextArea rows={3} placeholder="角色背景故事" /></Form.Item>
        </Form>
      </Modal>

      {/* 添加大纲弹窗：节点标题和摘要描述 */}
      <Modal title="添加大纲" open={outlineModal} onOk={handleAddOutline} onCancel={() => setOutlineModal(false)}>
        <Form form={outlineForm} layout="vertical">
          <Form.Item name="title" label="标题" rules={[{ required: true, message: '请输入标题' }]}><Input placeholder="大纲标题" /></Form.Item>
          <Form.Item name="summary" label="摘要"><TextArea rows={3} placeholder="大纲摘要" /></Form.Item>
        </Form>
      </Modal>

      {/* 添加世界观 Modal */}
      <Modal title="添加世界观设定" open={worldModal} onOk={handleAddWorld} onCancel={() => setWorldModal(false)} width={600}>
        <Form form={worldForm} layout="vertical">
          <Form.Item name="category" label="分类" rules={[{ required: true, message: '请选择分类' }]}><Input placeholder="geography / culture / magic_system / history / species" /></Form.Item>
          <Form.Item name="name" label="名称" rules={[{ required: true, message: '请输入名称' }]}><Input placeholder="设定名称" /></Form.Item>
          <Form.Item name="description" label="描述"><TextArea rows={3} placeholder="设定描述" /></Form.Item>
        </Form>
      </Modal>

      {/* AI 生成 Modal (原有) */}
      <Modal title={`AI 生成${aiModal === 'plot' ? '大纲' : aiModal === 'character' ? '角色' : aiModal === 'chapter' ? '章节' : '世界观'}`} open={!!aiModal} onOk={() => handleAiGenerate(aiModal!)} onCancel={() => setAiModal(null)} confirmLoading={aiLoading} okText="生成">
        <Form form={aiForm} layout="vertical">
          {aiModal === 'plot' && (<>
            <Form.Item name="prompt" label="故事构思" rules={[{ required: true }]}><TextArea rows={3} placeholder="描述你的故事构思" /></Form.Item>
            <Form.Item name="chapterCount" label="预计章节数" initialValue={20}><InputNumber min={5} max={100} style={{ width: '100%' }} /></Form.Item>
            <Form.Item name="genre" label="题材类型"><Input placeholder="如：玄幻、科幻、言情" /></Form.Item>
          </>)}
          {aiModal === 'character' && (<>
            <Form.Item name="prompt" label="角色描述" rules={[{ required: true }]}><TextArea rows={3} placeholder="描述你想要的角色" /></Form.Item>
            <Form.Item name="role" label="角色类型" initialValue="supporting"><Input placeholder="protagonist / antagonist / supporting" /></Form.Item>
          </>)}
          {aiModal === 'chapter' && (<>
            <Form.Item name="instructions" label="写作要求" rules={[{ required: true }]}><TextArea rows={3} placeholder="描述本章的内容要求" /></Form.Item>
            <Form.Item name="wordCount" label="目标字数" initialValue={3000}><InputNumber min={500} max={10000} step={500} style={{ width: '100%' }} /></Form.Item>
          </>)}
          {aiModal === 'world' && (<>
            <Form.Item name="category" label="分类" rules={[{ required: true }]}><Input placeholder="geography / culture / magic_system / history / species" /></Form.Item>
            <Form.Item name="prompt" label="描述" rules={[{ required: true }]}><TextArea rows={3} placeholder="描述你想要的世界观设定" /></Form.Item>
          </>)}
        </Form>
      </Modal>

      {/* 批量生成世界观 Modal */}
      <Modal title="AI 批量生成世界观" open={worldBatchModal} onOk={handleWorldBatchGenerate} onCancel={() => setWorldBatchModal(false)} confirmLoading={aiLoading} okText="批量生成">
        <Form form={worldBatchForm} layout="vertical">
          <Form.Item name="categories" label="分类列表（逗号分隔）" rules={[{ required: true, message: '请输入分类' }]}>
            <Input placeholder="magic_system, geography, culture, history, species" />
          </Form.Item>
          <Form.Item name="prompt" label="总体描述"><TextArea rows={2} placeholder="世界观整体描述（可选）" /></Form.Item>
          <Form.Item name="countPerCategory" label="每类条目数" initialValue={3}><InputNumber min={1} max={10} style={{ width: '100%' }} /></Form.Item>
        </Form>
      </Modal>

      {/* 建议分类 Modal */}
      <Modal title="AI 建议世界观分类" open={suggestCategoryModal} onCancel={() => setSuggestCategoryModal(false)} footer={<Button onClick={() => setSuggestCategoryModal(false)}>关闭</Button>}>
        {categoryResults.length === 0 ? <Empty description="获取建议中..." /> : (
          <List dataSource={categoryResults} renderItem={(item) => (
            <List.Item>
              <List.Item.Meta
                title={<Space>{item.category} <Tag>{item.priority}</Tag></Space>}
                description={item.reason}
              />
            </List.Item>
          )} />
        )}
      </Modal>

      {/* 分类建议触发前表单 Modal */}
      <Modal title="设置分类建议参数" open={!!(suggestForm && !suggestCategoryModal)} onOk={handleSuggestCategories} onCancel={() => {}} confirmLoading={categoryLoading} okText="获取建议" footer={null}>
      </Modal>

      {/* 一致性检查结果 Modal */}
      <Modal title="世界观一致性检查" open={consistencyModal} onCancel={() => setConsistencyModal(false)} footer={<Button onClick={() => setConsistencyModal(false)}>关闭</Button>} width={700}>
        {consistencyResults.length === 0 ? (
          <Alert message="未发现矛盾或不一致之处" type="success" showIcon />
        ) : (
          <List dataSource={consistencyResults} renderItem={(item) => (
            <List.Item>
              <List.Item.Meta
                avatar={<Tag color={severityColors[item.severity] || 'blue'}>{item.severity}</Tag>}
                title={item.issue}
                description={
                  <div>
                    <div><Text type="danger">涉及条目：</Text>{item.firstEntry}{item.secondEntry ? ` vs ${item.secondEntry}` : ''}</div>
                    <div><Text type="warning">建议：</Text>{item.suggestion}</div>
                  </div>
                }
              />
            </List.Item>
          )} />
        )}
      </Modal>

      {/* 扩展世界观 Modal */}
      <Modal title="AI 扩展世界观细节" open={expandWorldModal} onOk={handleExpandWorld} onCancel={() => setExpandWorldModal(false)} confirmLoading={expandWorldLoading} okText="扩展">
        <Form form={expandForm} layout="vertical">
          <Form.Item name="aspect" label="扩展方向" rules={[{ required: true, message: '请输入扩展方向' }]}>
            <TextArea rows={2} placeholder="希望扩展的方向，如：历史起源、内部运作机制、与其他设定的关联等" />
          </Form.Item>
          <Form.Item name="detailLevel" label="详细程度" initialValue="detailed">
            <Input placeholder="brief / detailed / very_detailed" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 审查大纲 Modal - 触发前参数 */}
      <Modal title="审查大纲参数" open={!!(reviewForm && !reviewPlotModal)} onOk={handleReviewPlot} onCancel={() => {}} confirmLoading={reviewLoading} okText="开始审查" footer={null}>
      </Modal>

      {/* 审查结果 Modal */}
      <Modal title="大纲审查结果" open={reviewPlotModal} onCancel={() => setReviewPlotModal(false)} footer={<Button onClick={() => setReviewPlotModal(false)}>关闭</Button>} width={800}>
        {plotReviewResult ? (
          <div>
            <Alert message={plotReviewResult.summary} type="info" style={{ marginBottom: 16 }} />
            {plotReviewResult.pacing && (
              <div style={{ marginBottom: 16 }}>
                <Text strong>节奏评价：</Text><Tag color={plotReviewResult.pacing.overallRating === '优秀' ? 'success' : 'warning'}>{plotReviewResult.pacing.overallRating}</Tag>
                <div style={{ marginTop: 8 }}>
                  {plotReviewResult.pacing.slowParts.length > 0 && <Text type="secondary">偏慢部分：{plotReviewResult.pacing.slowParts.join(', ')}</Text>}
                </div>
                <div>
                  {plotReviewResult.pacing.fastParts.length > 0 && <Text type="secondary">偏快部分：{plotReviewResult.pacing.fastParts.join(', ')}</Text>}
                </div>
                <div style={{ marginTop: 8 }}><Text>{plotReviewResult.pacing.recommendation}</Text></div>
              </div>
            )}
            {plotReviewResult.issues.length > 0 && (
              <div>
                <Text strong>发现的问题（{plotReviewResult.issues.length} 个）：</Text>
                <List dataSource={plotReviewResult.issues} renderItem={(issue) => (
                  <List.Item>
                    <List.Item.Meta
                      avatar={<Tag color={severityColors[issue.severity] || 'blue'}>{issue.severity}</Tag>}
                      title={`[${issue.type}] ${issue.description}`}
                      description={<div>{issue.outlineTitle && <Tag>{issue.outlineTitle}</Tag>} {issue.suggestion}</div>}
                    />
                  </List.Item>
                )} />
              </div>
            )}
            {plotReviewResult.suggestions.length > 0 && (
              <div style={{ marginTop: 16 }}>
                <Text strong>改进建议：</Text>
                <ul>{plotReviewResult.suggestions.map((s, i) => <li key={i}>{s}</li>)}</ul>
              </div>
            )}
          </div>
        ) : <Spin />}
      </Modal>

      {/* 扩展情节 Modal */}
      <Modal title="AI 扩展情节" open={expandPlotModal} onOk={handleExpandPlot} onCancel={() => setExpandPlotModal(false)} confirmLoading={expandPlotLoading} okText="扩展">
        <Form form={expandPlotForm} layout="vertical">
          <Form.Item name="focus" label="扩展类型" initialValue="subplot">
            <Input placeholder="subplot / detail / transition / climax" />
          </Form.Item>
          <Form.Item name="depth" label="情节节点数" initialValue={1}>
            <InputNumber min={1} max={5} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="outlineId" label="基于的大纲 ID（可选）">
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>

      {/* 分支建议触发前 */}
      <Modal title="AI 情节分支建议" open={branchModal} onCancel={() => setBranchModal(false)} footer={null} width={700}>
        {branches.length > 0 ? (
          <div>
            <Text strong>提供 {branches.length} 个分支方向：</Text>
            <List dataSource={branches} renderItem={(branch) => (
              <List.Item>
                <List.Item.Meta title={branch.title} description={<div><Text>{branch.summary}</Text><Divider /><Text type="secondary">影响：{branch.impact}</Text></div>} />
              </List.Item>
            )} />
          </div>
        ) : (
          <Form form={branchForm} layout="vertical">
            <Form.Item name="keyPoint" label="分支关键点" rules={[{ required: true }]}><TextArea rows={2} placeholder="描述情节的关键转折点" /></Form.Item>
            <Form.Item name="branchCount" label="分支数量" initialValue={3}><InputNumber min={2} max={5} style={{ width: '100%' }} /></Form.Item>
            <Button type="primary" icon={<BranchesOutlined />} onClick={handleSuggestBranches} loading={branchLoading} block>生成分支建议</Button>
          </Form>
        )}
      </Modal>

      {/* 角色弧线整合 Modal */}
      <Modal title="角色弧线整合分析" open={charArcModal} onCancel={() => setCharArcModal(false)} footer={<Button onClick={() => setCharArcModal(false)}>关闭</Button>} width={700}>
        {charArcResults.length > 0 ? (
          <List dataSource={charArcResults} renderItem={(item) => (
            <List.Item>
              <List.Item.Meta
                title={<Title level={5}>{item.characterName}</Title>}
                description={
                  <div>
                    <Divider />
                    <Text strong>当前弧线：</Text><Text>{item.currentArc}</Text>
                    <Divider />
                    <Text strong>建议：</Text>
                    <ul>{item.suggestions.map((s, i) => <li key={i}>{s}</li>)}</ul>
                    <Divider />
                    <Text strong>涉及的大纲：</Text>
                    <Space>{item.relatedOutlines.map((o, i) => <Tag key={i}>{o}</Tag>)}</Space>
                  </div>
                }
              />
            </List.Item>
          )} />
        ) : (
          <Form form={charArcForm} layout="vertical">
            <Form.Item name="characterId" label="聚焦特定角色 ID（可选）"><InputNumber min={1} style={{ width: '100%' }} /></Form.Item>
            <Button type="primary" icon={<ApartmentOutlined />} onClick={handleIntegrateCharacterArc} loading={charArcLoading} block>分析角色弧线</Button>
          </Form>
        )}
      </Modal>

      {/* 伏笔分析 Modal */}
      <Modal title="伏笔与回收分析" open={foreshadowModal} onCancel={() => setForeshadowModal(false)} footer={<Button onClick={() => setForeshadowModal(false)}>关闭</Button>} width={700}>
        {foreshadowResults ? (
          <div>
            <Alert message={foreshadowResults.analysis} type="info" style={{ marginBottom: 16 }} />
            <Text strong>伏笔建议（{foreshadowResults.suggestions.length} 条）：</Text>
            <List dataSource={foreshadowResults.suggestions} renderItem={(item) => (
              <List.Item>
                <List.Item.Meta
                  avatar={<Tag color={item.importance === 'high' ? 'red' : item.importance === 'medium' ? 'orange' : 'blue'}>{item.importance}</Tag>}
                  title={<Text strong>{item.hint}</Text>}
                  description={
                    <div>
                      <div><Text type="secondary">埋下伏笔：</Text>{item.foreshadowAt}</div>
                      <div><Text type="secondary">回收揭示：</Text>{item.payoffAt}</div>
                    </div>
                  }
                />
              </List.Item>
            )} />
          </div>
        ) : <Spin />}
      </Modal>
    </div>
  );
}

interface ForeshadowingReviewResult {
  analysis: string;
  suggestions: Array<{
    foreshadowAt: string;
    payoffAt: string;
    hint: string;
    importance: string;
  }>;
}
