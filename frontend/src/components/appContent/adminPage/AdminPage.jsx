import { useState, useEffect } from "react"

import adminGetUsersCall from "../../../services/api/adminGetUsersCall"
import adminBanUserCall from "../../../services/api/adminBanCall"
import adminPromoteUserCall from "../../../services/api/adminPromoteCall"

import PopupMessage from "../../popupMessage/PopupMessage"

import "./AdminPage.css"


export default function AdminPage() {
    const [users, setUsers] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState("");
    const [refreshFlag, setRefreshFlag] = useState(false);

    useEffect(() => {
        async function fetchUsers() {
            setIsLoading(true);
            try {
                const response = await adminGetUsersCall();
                setUsers(response.data);
            } catch (error) {
                setError("Failed to fetch users");
            } finally {
                setIsLoading(false);
            }
        }

        fetchUsers();
    }, [refreshFlag]);

    return (
        <div className="admin-page-container">
            <div className="admin-page-content-container">
                <h1>Admin Page</h1>
                <div className="admin-page-users-list">
                    {isLoading && <p className="users-list-loading-message">Loading users...</p>}
                    {error && <p className="users-list-error-message">{error}</p>}
                    {!isLoading && !error && users.map((user, index) => (
                        <UserCard key={index} user={user} />
                    ))}
                </div>
            </div>
        </div>
    )

    function UserCard({ user }) {
    const [showBanPopup, setShowBanPopup] = useState(false);
    const [showPromotePopup, setShowPromotePopup] = useState(false);

    return (
        <div className={`user-card-container`}>
            <div className="user-card-info">
                <div className="user-card-info-email"><strong>Email:</strong> {user.email}</div>
                <div className="user-card-info-role"><strong>Role:</strong> {user.role}</div>
                <div className="user-card-info-banned"><strong>Banned:</strong> {user.banned ? 'Yes' : 'No'}</div>
                <div className="user-card-info-createdAt"><strong>Created At:</strong> {new Date(user.createdAt).toLocaleString()}</div>
            </div>
            <div className="user-card-actions">
                <button className="dangerous-button user-card-actions-ban-button"
                    onClick={() => setShowBanPopup(true)}>
                    {user.banned ? 'Unban' : 'Ban'}
                    </button>
                <button className="dangerous-button user-card-actions-promote-button"
                    onClick={() => setShowPromotePopup(true)}>
                    {user.role === 'ADMIN' ? 'Demote to User' : 'Promote to Admin'}
                </button>
            </div>
            {showBanPopup && (
                <BanPopupMessage />
            )}
            {showPromotePopup && (
                <PromotePopupMessage />
            )}
        </div>
    )

    function BanPopupMessage() {
        const handleBanUser = async () => {
            try {
                await adminBanUserCall(user.userId, !user.banned);
                setRefreshFlag(!refreshFlag);
            } catch (error) {
                console.error("Failed to ban/unban user:", error);
            } finally {
                setShowBanPopup(false);
            }
        };

        return (
            <PopupMessage onClickOutside={() => setShowBanPopup(false)}>
                <div className="user-card-popup-message-content">
                    <h2>Are you sure</h2>
                    <h2>you want to {user.banned ? 'unban' : 'ban'} this user?</h2>
                    <div className="user-card-popup-buttons-container">
                        <button 
                            onClick={() => setShowBanPopup(false)}>
                            Cancel
                        </button>
                        <button 
                            className="dangerous-button"
                            onClick={handleBanUser}>
                            {user.banned ? 'Unban' : 'Ban'}
                        </button>
                    </div>
                </div>
            </PopupMessage>
        );
    }

    function PromotePopupMessage() {
        const handlePromoteUser = async () => {
            try {
                const newRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN';
                await adminPromoteUserCall(user.userId, newRole);
                setRefreshFlag(!refreshFlag);
            } catch (error) {
                console.error("Failed to promote/demote user:", error);
            } finally {
                setShowPromotePopup(false);
            }
        };

        return (
            <PopupMessage onClickOutside={() => setShowPromotePopup(false)}>
                <div className="user-card-popup-message-content">
                    <h2>Are you sure</h2>
                    <h2>you want to {user.role === 'ADMIN' ? 'demote' : 'promote'} this user?</h2>
                    <div className="user-card-popup-buttons-container">
                        <button 
                            onClick={() => setShowPromotePopup(false)}>
                            Cancel
                        </button>
                        <button 
                            className="dangerous-button"
                            onClick={handlePromoteUser}>
                            {user.role === 'ADMIN' ? 'Demote' : 'Promote'}
                        </button>
                    </div>
                </div>
            </PopupMessage>
        );
    }
}
}
