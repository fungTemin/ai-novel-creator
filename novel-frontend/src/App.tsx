// 应用路由配置：定义公开路由（登录/注册）和受保护路由（需要认证的页面）
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from './stores/authStore';
import MainLayout from './components/Layout/MainLayout';
import Login from './pages/Login';
import Register from './pages/Register';
import NovelList from './pages/NovelList';
import NovelDashboard from './pages/NovelDashboard';
import ChapterEditor from './pages/ChapterEditor';
import StoryBuilderPage from './pages/StoryBuilderPage';

// 路由守卫组件：未登录时重定向到 /login
function PrivateRoute({ children }: { children: React.ReactNode }) {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated());
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />;
}

function App() {
  return (
    <Routes>
      {/* 公开路由：无需登录即可访问 */}
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      {/* 受保护路由：所有需要登录的页面都包裹在 PrivateRoute + MainLayout 内 */}
      <Route
        path="/"
        element={
          <PrivateRoute>
            <MainLayout />
          </PrivateRoute>
        }
      >
        <Route index element={<Navigate to="/novels" replace />} />
        <Route path="novels" element={<NovelList />} />
        <Route path="novels/:id" element={<NovelDashboard />} />
        <Route path="novels/:id/builder" element={<StoryBuilderPage />} />
        <Route path="novels/:novelId/chapters/:chapterId" element={<ChapterEditor />} />
      </Route>
    </Routes>
  );
}

export default App;
