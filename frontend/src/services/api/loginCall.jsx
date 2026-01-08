import apiClient from "./apiClient";

export default async function loginCall(email, password) {
  return apiClient.post("/auth/login", { email, password });
}
