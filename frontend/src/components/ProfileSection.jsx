"use client";

import React from 'react';
import PropTypes from 'prop-types';

const ProfileSection = ({ title, images }) => {
  return (
    
    <div className="text-lg md:text-xl font-bold mb-4 font-poppins text-gray-800">
      <h2 className="text-lg md:text-xl font-bold mb-4 font-poppins text-gray-800">
        {title}
      </h2>
      <div className="flex flex-col items-center justify-center">
        <div className="flex flex-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
          {images.map((image, index) => (
            <div
              key={index}
              className="w-full h-40 bg-gray-200 rounded-lg flex items-center justify-center overflow-hidden"
            >
              <img
                src={image}
                alt={`Imagem ${index + 1}`}
                className="max-w-full max-h-full rounded-lg"
              />
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

ProfileSection.propTypes = {
  title: PropTypes.string.isRequired,
  images: PropTypes.arrayOf(PropTypes.string).isRequired,
};

export default ProfileSection;