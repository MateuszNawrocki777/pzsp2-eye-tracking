import apiClient from "./apiClient";

export default async function adminPromoteCall(userId, role) {
    return apiClient.post(`/admin/users/${userId}/role`, { role });
}
