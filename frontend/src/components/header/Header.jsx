import { NavLink } from 'react-router-dom'

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
    const { isLoggedIn, logout } = useAuth();

    return (
        <div className='header-navigation-div'>
            <NavLink to="/" className={"header-navigation-button"}>Home</NavLink>
            <NavLink to="/about" className={"header-navigation-button"}>About</NavLink>
            <NavLink to="/newTest" className={"header-navigation-button"}>New Test</NavLink>
            {isLoggedIn && <NavLink to="/myTests" className={"header-navigation-button"}>My Tests</NavLink>}
            {isLoggedIn ? (
                <a onClick={logout} className={"header-navigation-button"}>Logout</a> //TODO: Implement logging out
            ) : (
                <NavLink to="/login" className={"header-navigation-login-button"}>Login</NavLink>
            )}
        </div>
    )
}
