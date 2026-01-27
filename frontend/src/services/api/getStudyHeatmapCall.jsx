import apiClient from "./apiClient";

export default async function getStudyHeatmapCall(sessionId) {
  return apiClient.get(`/api/sessions/${sessionId}`);
}
