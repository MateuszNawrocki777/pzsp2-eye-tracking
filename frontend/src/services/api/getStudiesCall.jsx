import apiClient from "./apiClient";

export default async function getStudiesCall(testId) {
  return apiClient.get(`/api/sessions/test/${testId}`);
}
