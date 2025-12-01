import apiClient from "./apiClient";

export default async function adminBanCall(userId, banned) {
    return apiClient.post(`/admin/users/${userId}/banned`, { banned });
}
