/**
 * Service de génération de rapports
 */

import { apiClient } from '@/lib/api-client'
import { API_CONFIG } from '@/lib/api-config'

export interface Report {
  id: string
  title: string
  type: 'usage' | 'attendance' | 'performance' | 'custom'
  generatedBy: string
  generatedAt: Date
  data: any
}

export interface GenerateReportRequest {
  type: 'usage' | 'attendance' | 'performance' | 'custom'
  startDate?: string
  endDate?: string
  filters?: Record<string, any>
}

class ReportService {
  /**
   * Récupérer tous les rapports
   */
  async getReports(): Promise<Report[]> {
    return apiClient.get<Report[]>(API_CONFIG.endpoints.reports.base)
  }

  /**
   * Générer un nouveau rapport
   */
  async generateReport(data: GenerateReportRequest): Promise<Report> {
    return apiClient.post<Report>(API_CONFIG.endpoints.reports.generate, data)
  }

  /**
   * Télécharger un rapport en PDF
   */
  async downloadReport(reportId: string): Promise<Blob> {
    const response = await fetch(
      `${API_CONFIG.baseURL}${API_CONFIG.endpoints.reports.base}/${reportId}/download`,
      {
        headers: {
          'Authorization': `Bearer ${apiClient.getAccessToken()}`,
        },
      }
    )

    if (!response.ok) {
      throw new Error('Failed to download report')
    }

    return response.blob()
  }
}

export const reportService = new ReportService()
