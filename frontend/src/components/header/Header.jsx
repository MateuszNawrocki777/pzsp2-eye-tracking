import { NavLink } from 'react-router-dom'

import './Header.css'


export default function Header() {
    return (
        <div className="header-container">
            <div className='header-logo-div'>Logo placeholder</div>
            <div className='header-navigation-div'>
                <NavLink to="/">Home</NavLink>
                <NavLink to="/about">About</NavLink>
                <NavLink to="/profile">Profile</NavLink>
            </div>
        </div>
    )
}
