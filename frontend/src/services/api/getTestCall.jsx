import apiClient from "./apiClient";

export default async function getTestCall(testId) {
  return apiClient.get(`/api/tests/${testId}`);
}
