import { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from 'react';
import type { UserInfo, AuthResponse } from '../types';
import { authApi } from '../api/auth';
import { setTokens, clearTokens, loadRefreshToken } from '../api/client';

interface AuthState {
  user: UserInfo | null;
  isLoading: boolean;
  isAuthenticated: boolean;
}

interface AuthContextType extends AuthState {
  login: (email: string, password: string) => Promise<void>;
  register: (data: {
    organizationName: string;
    ownerName: string;
    email: string;
    password: string;
    phone?: string;
  }) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>({
    user: null,
    isLoading: true,
    isAuthenticated: false,
  });

  // Try to restore session from refresh token on mount
  useEffect(() => {
    const storedRefresh = loadRefreshToken();
    if (storedRefresh) {
      authApi
        .refresh(storedRefresh)
        .then((res: AuthResponse) => {
          setTokens(res.accessToken, res.refreshToken);
          setState({ user: res.user, isLoading: false, isAuthenticated: true });
        })
        .catch(() => {
          clearTokens();
          setState({ user: null, isLoading: false, isAuthenticated: false });
        });
    } else {
      setState({ user: null, isLoading: false, isAuthenticated: false });
    }
  }, []);

  const handleAuth = useCallback((res: AuthResponse) => {
    setTokens(res.accessToken, res.refreshToken);
    setState({ user: res.user, isLoading: false, isAuthenticated: true });
  }, []);

  const login = useCallback(
    async (email: string, password: string) => {
      const res = await authApi.login({ email, password });
      handleAuth(res);
    },
    [handleAuth]
  );

  const register = useCallback(
    async (data: {
      organizationName: string;
      firstName: string;
      lastName: string;
      email: string;
      password: string;
      phone?: string;
    }) => {
      const res = await authApi.register(data);
      handleAuth(res);
    },
    [handleAuth]
  );

  const logout = useCallback(async () => {
    const storedRefresh = loadRefreshToken();
    if (storedRefresh) {
      try {
        await authApi.logout(storedRefresh);
      } catch {
        // ignore logout errors
      }
    }
    clearTokens();
    setState({ user: null, isLoading: false, isAuthenticated: false });
  }, []);

  return (
    <AuthContext.Provider value={{ ...state, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
