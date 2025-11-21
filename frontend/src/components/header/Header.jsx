import { NavLink } from 'react-router-dom'

import { useAuth } from '../../hooks/authContext.jsx'

import './Header.css'


export default function Header() {

    return (
        <div className="header-container">
            <div className='header-logo-div'>Logo placeholder</div>
            <Navigation />
        </div>
    )
}


function Navigation() {
    const { isLoggedIn, login, logout } = useAuth();

    return (
        <div className='header-navigation-div'>
            <NavLink to="/" className={"header-navigation-button"}>Home</NavLink>
            <NavLink to="/about" className={"header-navigation-button"}>About</NavLink>
            <NavLink to="/profile" className={"header-navigation-button"}>Profile</NavLink>
            {isLoggedIn ? ( 
                <button onClick={logout} className={"header-navigation-button"}>Logout</button> //TODO: These buttons arent styled properly
            ) : (
                <button onClick={login} className={"header-navigation-login-button"}>Login</button>
            )}
        </div>
    )
}
