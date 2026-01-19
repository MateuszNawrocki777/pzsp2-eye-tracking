import apiClient from "./apiClient";

export default async function submitStudyCall(study_id, name, points_per_image) {
  return apiClient.post(`/api/sessions`, { study_id, name, points_per_image });
}
