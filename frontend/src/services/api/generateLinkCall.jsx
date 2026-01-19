import apiClient from "./apiClient";

export default async function generateLinkCall(testId, max_uses, expires_at) {
  return apiClient.post(`/api/tests/${testId}/share`, { max_uses, expires_at });
}
