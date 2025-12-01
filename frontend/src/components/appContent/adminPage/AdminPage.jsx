import { useState } from "react"

import PopupMessage from "../../popupMessage/PopupMessage"

import "./AdminPage.css"



const EXAMPLE_USERS = [
  {
    "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "email": "string",
    "role": "USER",
    "banned": true,
    "createdAt": "2025-12-01T00:16:29.166Z"
  },
  {
    "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "email": "string",
    "role": "USER",
    "banned": true,
    "createdAt": "2025-12-01T00:16:29.166Z"
  },
  {
    "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "email": "string",
    "role": "ADMIN",
    "banned": false,
    "createdAt": "2025-12-01T00:16:29.166Z"
  },
  {
    "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "email": "string",
    "role": "USER",
    "banned": true,
    "createdAt": "2025-12-01T00:16:29.166Z"
  },
  {
    "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "email": "string",
    "role": "USER",
    "banned": true,
    "createdAt": "2025-12-01T00:16:29.166Z"
  },
]


export default function AdminPage() {
    return (
        <div className="admin-page-container">
            <div className="admin-page-content-container">
                <h1>Admin Page</h1>
                <div className="admin-page-users-list">
                    {EXAMPLE_USERS.map((user, index) => (
                        <UserCard key={index} user={user} />
                    ))}
                </div>
            </div>
        </div>
    )
}


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
                            onClick={() => {console.log(`${user.banned ? 'Unbanning' : 'Banning'} user ${user.email}`); setShowBanPopup(false);}}>
                            {user.banned ? 'Unban' : 'Ban'}
                        </button>
                    </div>
                </div>
            </PopupMessage>
        );
    }

    function PromotePopupMessage() {
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
                            onClick={() => {console.log(`${user.role === 'ADMIN' ? 'Demoting' : 'Promoting'} user ${user.email}`); setShowPromotePopup(false);}}>
                            {user.role === 'ADMIN' ? 'Demote' : 'Promote'}
                        </button>
                    </div>
                </div>
            </PopupMessage>
        );
    }
}
