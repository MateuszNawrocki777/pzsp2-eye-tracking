import { NavLink, useNavigate } from 'react-router-dom'


import { useAuth } from '../../hooks/authContext.jsx'

import './Header.css'


export default function Header() {

    return (
        <div className="header-container">
            <div className='header-logo-div'>
                <img src={"./logo.png"} alt="Logo" className='header-logo-image'/>
                <h2>EyeTracking</h2>
            </div>
            <Navigation />
        </div>
    )
}


function Navigation() {
    const { isLoggedIn, logout, role } = useAuth();
    const navigate = useNavigate();

    return (
        <div className='header-navigation-div'>
            <NavLink to="/" className={"header-navigation-button"}>Home</NavLink>
            <NavLink to="/newTest" className={"header-navigation-button"}>New Test</NavLink>
            {!isLoggedIn && <NavLink to="/myTests" className={"header-navigation-button"}>My Tests</NavLink>}
            {role === "ADMIN" && <NavLink to="/admin" className={"header-navigation-button"}>Admin</NavLink>}
            {isLoggedIn ? (
                <a
                    onClick={() => {logout(); navigate("/");}}
                    className={"header-navigation-button"}>
                        Logout
                </a>
            ) : (
                <NavLink to="/login" className={"header-navigation-login-button"}>Login</NavLink>
            )}
        </div>
    )
}
