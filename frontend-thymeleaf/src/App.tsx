import React, { ReactNode } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './components/auth/LoginPage';
import TimeTableScheduling from './components/schedule/TimeTableSchedule';
import UserManagement from './components/users/UserManagement';
import ProfilePage from './components/profile/ProfilePage';

interface ProtectedRouteProps {
  children: ReactNode;
}

function ProtectedRoute({ children }: ProtectedRouteProps) {
  const token = localStorage.getItem('token');
  return token ? <>{children}</> : <Navigate to="/login" />;
}

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route 
          path="/schedule" 
          element={
            <ProtectedRoute>
              <TimeTableScheduling />
            </ProtectedRoute>
          } 
        />
        <Route 
          path="/users" 
          element={
            <ProtectedRoute>
              <UserManagement />
            </ProtectedRoute>
          } 
        />
        <Route 
          path="/profile" 
          element={
            <ProtectedRoute>
              <ProfilePage />
            </ProtectedRoute>
          } 
        />
        <Route path="/" element={<Navigate to="/login" />} />
      </Routes>
    </Router>
  );
}

export default App;
