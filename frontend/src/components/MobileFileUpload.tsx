import React, { useRef, useState } from 'react';
import { isMobile, takePhoto, pickImage, base64ToFile } from '../utils/mobile';

interface MobileFileUploadProps {
  onFilesSelected: (files: FileList | null) => void;
  uploadStatus: 'idle' | 'uploading' | 'success' | 'error';
}

const MobileFileUpload: React.FC<MobileFileUploadProps> = ({ onFilesSelected, uploadStatus }) => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [showMobileOptions, setShowMobileOptions] = useState(false);

  const handleFileInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    onFilesSelected(e.target.files);
    e.target.value = ''; // Reset input
  };

  const handleCameraCapture = async () => {
    const photo = await takePhoto();
    if (photo) {
      // Convert base64 to File using helper function
      const file = base64ToFile(
        photo.data,
        `contract_${Date.now()}.${photo.format}`,
        `image/${photo.format}`
      );

      // Create a FileList-like object
      const dataTransfer = new DataTransfer();
      dataTransfer.items.add(file);
      onFilesSelected(dataTransfer.files);
    }
    setShowMobileOptions(false);
  };

  const handleGalleryPick = async () => {
    const photo = await pickImage();
    if (photo) {
      // Convert base64 to File using helper function
      const file = base64ToFile(
        photo.data,
        `contract_${Date.now()}.${photo.format}`,
        `image/${photo.format}`
      );

      // Create a FileList-like object
      const dataTransfer = new DataTransfer();
      dataTransfer.items.add(file);
      onFilesSelected(dataTransfer.files);
    }
    setShowMobileOptions(false);
  };

  const handleFileBrowser = () => {
    fileInputRef.current?.click();
    setShowMobileOptions(false);
  };

  if (!isMobile()) {
    // Desktop version - simple file input
    return (
      <div className="upload-section">
        <h3>Upload File</h3>
        <input
          type="file"
          ref={fileInputRef}
          onChange={handleFileInputChange}
          disabled={uploadStatus === 'uploading'}
        />
        {uploadStatus === 'uploading' && <p>Uploading...</p>}
        {uploadStatus === 'success' && <p className="upload-success">✓ Upload successful</p>}
        {uploadStatus === 'error' && <p className="upload-error">✗ Upload failed</p>}
      </div>
    );
  }

  // Mobile version - with camera and gallery options
  return (
    <div className="upload-section mobile">
      <h3>Upload File</h3>
      
      {!showMobileOptions ? (
        <button 
          className="upload-button"
          onClick={() => setShowMobileOptions(true)}
          disabled={uploadStatus === 'uploading'}
        >
          📤 Choose File
        </button>
      ) : (
        <div className="mobile-options">
          <button 
            className="mobile-option-button camera"
            onClick={handleCameraCapture}
          >
            📷 Take Photo
          </button>
          <button 
            className="mobile-option-button gallery"
            onClick={handleGalleryPick}
          >
            🖼️ Choose from Gallery
          </button>
          <button 
            className="mobile-option-button files"
            onClick={handleFileBrowser}
          >
            📁 Browse Files
          </button>
          <button 
            className="mobile-option-button cancel"
            onClick={() => setShowMobileOptions(false)}
          >
            ✕ Cancel
          </button>
        </div>
      )}

      {/* Hidden file input for fallback */}
      <input
        type="file"
        ref={fileInputRef}
        onChange={handleFileInputChange}
        style={{ display: 'none' }}
        disabled={uploadStatus === 'uploading'}
      />

      {uploadStatus === 'uploading' && (
        <p className="upload-status">⏳ Uploading...</p>
      )}
      {uploadStatus === 'success' && (
        <p className="upload-status success">✓ Upload successful</p>
      )}
      {uploadStatus === 'error' && (
        <p className="upload-status error">✗ Upload failed</p>
      )}

      <style>{`
        .upload-section.mobile {
          margin: 1rem 0;
        }

        .upload-button {
          padding: 0.75rem 1.5rem;
          font-size: 1rem;
          background: #007bff;
          color: white;
          border: none;
          border-radius: 8px;
          cursor: pointer;
          width: 100%;
          max-width: 300px;
        }

        .upload-button:disabled {
          background: #6c757d;
          cursor: not-allowed;
        }

        .mobile-options {
          display: flex;
          flex-direction: column;
          gap: 0.5rem;
          margin-top: 0.5rem;
        }

        .mobile-option-button {
          padding: 0.75rem 1rem;
          font-size: 0.95rem;
          border: 2px solid #dee2e6;
          border-radius: 8px;
          background: white;
          cursor: pointer;
          text-align: left;
          transition: all 0.2s;
          width: 100%;
          max-width: 300px;
        }

        .mobile-option-button:hover,
        .mobile-option-button:active {
          background: #f8f9fa;
          border-color: #007bff;
        }

        .mobile-option-button.camera {
          border-color: #28a745;
        }

        .mobile-option-button.gallery {
          border-color: #17a2b8;
        }

        .mobile-option-button.files {
          border-color: #ffc107;
        }

        .mobile-option-button.cancel {
          border-color: #dc3545;
          color: #dc3545;
        }

        .upload-status {
          margin-top: 0.5rem;
          font-size: 0.9rem;
        }

        .upload-status.success {
          color: #28a745;
        }

        .upload-status.error {
          color: #dc3545;
        }
      `}</style>
    </div>
  );
};

export default MobileFileUpload;
