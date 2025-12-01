import apiClient from "./apiClient";

export default async function adminBanUserCall(userId, banned) {
    return apiClient.post(`/admin/users/${userId}/banned`, { banned });
}
