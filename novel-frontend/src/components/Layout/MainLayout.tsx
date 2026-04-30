// 主布局组件：提供全局导航栏（顶部 Header + 侧边 Sider）和内容区渲染出口
import { Layout, Menu, Button, Space, Typography } from 'antd';
import { BookOutlined, LogoutOutlined, UserOutlined } from '@ant-design/icons';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../stores/authStore';

const { Header, Content, Sider } = Layout;
const { Text } = Typography;

export default function MainLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuthStore();

  // 登出：清除认证状态并跳转到登录页
  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  // 侧边栏菜单项配置
  const menuItems = [
    {
      key: '/novels',
      icon: <BookOutlined />,
      label: '我的小说',
    },
  ];

  return (
    <Layout style={{ minHeight: '100vh' }}>
      {/* 顶部导航栏：Logo + 系统名称 + 当前用户信息 + 退出按钮 */}
      <Header style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '0 24px',
        background: '#001529',
      }}>
        <div style={{ display: 'flex', alignItems: 'center' }}>
          <BookOutlined style={{ color: '#fff', fontSize: '20px', marginRight: '12px' }} />
          <Text style={{ color: '#fff', fontSize: '18px', fontWeight: 600 }}>
            AI 小说创作系统
          </Text>
        </div>
        <Space>
          <Text style={{ color: '#fff' }}>
            <UserOutlined /> {user?.username}
          </Text>
          <Button
            type="text"
            icon={<LogoutOutlined />}
            onClick={handleLogout}
            style={{ color: '#fff' }}
          >
            退出
          </Button>
        </Space>
      </Header>
      <Layout>
        {/* 侧边栏：应用菜单导航 */}
        <Sider width={200} style={{ background: '#fff' }}>
          <Menu
            mode="inline"
            selectedKeys={[location.pathname]}
            items={menuItems}
            onClick={({ key }) => navigate(key)}
            style={{ height: '100%', borderRight: 0 }}
          />
        </Sider>
        {/* 内容区：通过 Outlet 渲染当前路由对应的子页面 */}
        <Layout style={{ padding: '24px' }}>
          <Content style={{
            background: '#fff',
            padding: 24,
            margin: 0,
            minHeight: 280,
            borderRadius: '8px',
          }}>
            <Outlet />
          </Content>
        </Layout>
      </Layout>
    </Layout>
  );
}
