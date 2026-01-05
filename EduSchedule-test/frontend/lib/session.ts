export interface SessionConfig {
  storageKey: string
  rememberMeDuration: number // in milliseconds
  sessionTimeout: number // in milliseconds
}

const DEFAULT_CONFIG: SessionConfig = {
  storageKey: "eduSchedule_session",
  rememberMeDuration: 30 * 24 * 60 * 60 * 1000, // 30 days
  sessionTimeout: 24 * 60 * 60 * 1000, // 24 hours
}

export function saveSession(data: any, rememberMe = false) {
  const sessionData = {
    data,
    timestamp: Date.now(),
    rememberMe,
    expiresAt: Date.now() + (rememberMe ? DEFAULT_CONFIG.rememberMeDuration : DEFAULT_CONFIG.sessionTimeout),
  }
  localStorage.setItem(DEFAULT_CONFIG.storageKey, JSON.stringify(sessionData))
}

export function getSession() {
  const stored = localStorage.getItem(DEFAULT_CONFIG.storageKey)
  if (!stored) return null

  try {
    const session = JSON.parse(stored)

    if (session.expiresAt && Date.now() > session.expiresAt) {
      clearSession()
      return null
    }

    return session.data
  } catch (error) {
    console.error("[v0] Failed to parse session:", error)
    clearSession()
    return null
  }
}

export function clearSession() {
  localStorage.removeItem(DEFAULT_CONFIG.storageKey)
}

export function isSessionValid(): boolean {
  const session = getSession()
  return !!session
}
