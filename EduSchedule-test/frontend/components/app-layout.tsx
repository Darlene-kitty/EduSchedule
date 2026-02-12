"use client"

import type React from "react"
import { Sidebar } from "./sidebar"
import { Header } from "./header"

interface AppLayoutProps {
  children: React.ReactNode
  activePage: string
  title: string
  subtitle?: string
  action?: React.ReactNode
}

export function AppLayout({ children, activePage, title, subtitle, action }: AppLayoutProps) {
  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activePage={activePage} />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Header title={title} subtitle={subtitle} action={action} />
        <main className="flex-1 overflow-y-auto p-6">
          {children}
        </main>
      </div>
    </div>
  )
}