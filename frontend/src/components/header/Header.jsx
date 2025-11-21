import { NavLink } from 'react-router-dom'

import { useAuth } from '../../hooks/authContext.jsx'

import './Header.css'


export default function Header() {
    const { isLoggedIn, login, logout } = useAuth();

    return (
        <div className="header-container">
            <div className='header-logo-div'>Logo placeholder</div>
            <div className='header-navigation-div'>
                <NavLink to="/">Home</NavLink>
                <NavLink to="/about">About</NavLink>
                <NavLink to="/profile">Profile</NavLink>
                {isLoggedIn ? (
                    <button onClick={logout}>Logout</button>
                ) : (
                    <button onClick={login}>Login</button>
                )}
            </div>
        </div>
    )
}
