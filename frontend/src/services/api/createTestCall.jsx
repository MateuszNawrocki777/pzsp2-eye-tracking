import apiClient from "./apiClient";

export default async function createTestCall(formData) {
  return apiClient.post("/api/tests", formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
}
