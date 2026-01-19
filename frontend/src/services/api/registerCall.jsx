import apiClient from "./apiClient";

export default async function registerCall(email, password) {
  return apiClient.post("api/auth/register", { email, password });
}
