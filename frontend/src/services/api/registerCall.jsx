import apiClient from "./apiClient";

export default async function registerCall(email, password) {
  return apiClient.post("/auth/register", { email, password });
}
