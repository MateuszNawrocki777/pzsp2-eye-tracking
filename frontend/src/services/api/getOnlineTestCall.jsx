import apiClient from "./apiClient";

export default async function getOnlineTestCall(accessId) {
  return apiClient.get(`/api/tests/share/${accessId}`);
}
