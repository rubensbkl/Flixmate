"use client"

import React from 'react'
import "@/styles/Header.css"
import PersonIcon from '@mui/icons-material/Person';
import ForumIcon from '@mui/icons-material/Forum';
import IconButton from '@mui/material/IconButton';

function Header() {
    return (
        <header className="header">
            <IconButton>
                <PersonIcon className="header__icon" fontSize="large" />
            </IconButton>
            <img
                src="https://1000logos.net/wp-content/uploads/2018/07/Tinder-icon.png"
                alt="Tinder logo"
            />
            <IconButton>
                <ForumIcon className="header__icon" fontSize="large" />
            </IconButton>
        </header>
    )
}

export default Header