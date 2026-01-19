import apiClient from "./apiClient";

export default async function deleteTestCall(testId) {
  return apiClient.delete(`/api/tests/${testId}`);
}
