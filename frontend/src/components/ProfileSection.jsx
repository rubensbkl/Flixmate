"use client";

import React from 'react';
import PropTypes from 'prop-types';

const ProfileSection = ({ title, images }) => {
  return (
    <div style={{ marginBottom: '2rem' }}>
      <h2
        style={{
          fontSize: '1.5rem', // Fonte um pouco menor
          fontWeight: 'bold',
          marginBottom: '1rem',
          fontFamily: "'Poppins', sans-serif", // Fonte estilizada
          color: '#333',
        }}
      >
        {title}
      </h2>
      <div
        style={{
          display: 'flex',
          gap: '1rem', // Espaçamento entre as imagens
          overflowX: 'auto', // Permite rolagem horizontal, se necessário
        }}
      >
        {images.map((image, index) => (
          <div
            key={index}
            style={{
              flex: '0 0 auto',
              width: '120px', // Largura menor
              height: '160px', // Altura menor
              backgroundColor: '#f0f0f0',
              borderRadius: '8px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              overflow: 'hidden',
            }}
          >
            <img
              src={image}
              alt={`Imagem ${index + 1}`}
              style={{
                maxWidth: '100%',
                maxHeight: '100%',
                borderRadius: '8px',
              }}
            />
          </div>
        ))}
      </div>
    </div>
  );
};

ProfileSection.propTypes = {
  title: PropTypes.string.isRequired,
  images: PropTypes.arrayOf(PropTypes.string).isRequired,
};

export default ProfileSection;