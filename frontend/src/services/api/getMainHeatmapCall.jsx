import apiClient from "./apiClient";

export default async function getMainHeatmapCall(testId) {
  return apiClient.get(`/api/sessions/test/${testId}/heatmap`);
}
