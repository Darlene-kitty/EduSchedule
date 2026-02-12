import type React from "react"
import type { Metadata } from "next"
import { Analytics } from "@vercel/analytics/next"
import "./globals.css"
import { Providers } from "@/components/providers"
import { ToastContainer } from "@/components/toast-container"

export const metadata: Metadata = {
  title: "EduSchedule - Gestion Intelligente",
  description: "Application de gestion des emplois du temps et réservations de salles",
  generator: "v0.app",
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  return (
    <html lang="fr">
      <body className="font-sans antialiased">
        <Providers>
          {children}
          <ToastContainer />
        </Providers>
        <Analytics />
      </body>
    </html>
  )
}
