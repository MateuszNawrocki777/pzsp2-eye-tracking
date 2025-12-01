import apiClient from "./apiClient";

export default async function adminGetUsersCall() {
    return apiClient.get("/admin/users");
}
