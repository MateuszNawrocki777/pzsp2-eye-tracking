import apiClient from "./apiClient";

export default async function getTestsCall() {
  return apiClient.get("/api/tests");
}
