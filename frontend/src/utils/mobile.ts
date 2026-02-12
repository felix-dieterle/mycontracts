import { Camera, CameraResultType, CameraSource } from '@capacitor/camera';
import { Filesystem, Directory } from '@capacitor/filesystem';
import { Share } from '@capacitor/share';
import { Capacitor } from '@capacitor/core';

/**
 * Check if the app is running on a native mobile platform (iOS or Android)
 */
export const isMobile = (): boolean => {
  return Capacitor.isNativePlatform();
};

/**
 * Get the platform the app is running on
 */
export const getPlatform = (): string => {
  return Capacitor.getPlatform();
};

/**
 * Take a photo using the device camera
 * @returns Base64 encoded image data and format
 */
export const takePhoto = async (): Promise<{ data: string; format: string } | null> => {
  try {
    const image = await Camera.getPhoto({
      quality: 90,
      allowEditing: false,
      resultType: CameraResultType.Base64,
      source: CameraSource.Camera,
    });

    if (image.base64String) {
      return {
        data: image.base64String,
        format: image.format,
      };
    }
    return null;
  } catch (error) {
    console.error('Error taking photo:', error);
    return null;
  }
};

/**
 * Pick an image from the gallery
 * @returns Base64 encoded image data and format
 */
export const pickImage = async (): Promise<{ data: string; format: string } | null> => {
  try {
    const image = await Camera.getPhoto({
      quality: 90,
      allowEditing: false,
      resultType: CameraResultType.Base64,
      source: CameraSource.Photos,
    });

    if (image.base64String) {
      return {
        data: image.base64String,
        format: image.format,
      };
    }
    return null;
  } catch (error) {
    console.error('Error picking image:', error);
    return null;
  }
};

/**
 * Pick multiple files from the device
 * @returns Array of File objects
 */
export const pickFiles = async (): Promise<File[]> => {
  // On mobile, use the Camera plugin to pick from photos
  // For document files, we'll fallback to standard HTML input
  if (!isMobile()) {
    return [];
  }

  try {
    const image = await Camera.getPhoto({
      quality: 90,
      allowEditing: false,
      resultType: CameraResultType.Base64,
      source: CameraSource.Photos,
    });

    if (image.base64String) {
      // Convert base64 to File object using helper function
      const file = base64ToFile(
        image.base64String,
        `photo_${Date.now()}.${image.format}`,
        `image/${image.format}`
      );
      return [file];
    }
  } catch (error) {
    console.error('Error picking files:', error);
  }

  return [];
};

/**
 * Share a file using the native share dialog
 * @param url URL to the file to share
 * @param title Title for the share dialog
 * 
 * Note: The Share API works best with files that are already downloaded or accessible.
 * For remote URLs, some platforms may require the file to be downloaded first.
 * Consider using downloadFile() before shareFile() for remote resources.
 */
export const shareFile = async (url: string, title: string = 'Share File'): Promise<boolean> => {
  try {
    await Share.share({
      title: title,
      url: url,
      dialogTitle: title,
    });
    return true;
  } catch (error) {
    console.error('Error sharing file:', error);
    return false;
  }
};

/**
 * Download a file to the device
 * @param url URL to download from
 * @param filename Name for the downloaded file
 */
export const downloadFile = async (url: string, filename: string): Promise<boolean> => {
  if (!isMobile()) {
    // On web, use standard download
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    return true;
  }

  try {
    // Fetch the file
    const response = await fetch(url);
    const blob = await response.blob();
    const reader = new FileReader();

    return new Promise((resolve, reject) => {
      reader.onloadend = async () => {
        try {
          const base64Data = (reader.result as string).split(',')[1];
          
          // Save to filesystem
          await Filesystem.writeFile({
            path: filename,
            data: base64Data,
            directory: Directory.Documents,
          });

          console.log('File downloaded to Documents folder:', filename);
          resolve(true);
        } catch (error) {
          console.error('Error saving file:', error);
          reject(false);
        }
      };

      reader.onerror = () => {
        reject(false);
      };

      reader.readAsDataURL(blob);
    });
  } catch (error) {
    console.error('Error downloading file:', error);
    return false;
  }
};

/**
 * Convert base64 to File object
 * @param base64 Base64 encoded data
 * @param filename Name for the file
 * @param mimeType MIME type of the file
 */
export const base64ToFile = (base64: string, filename: string, mimeType: string): File => {
  const byteCharacters = atob(base64);
  const byteNumbers = new Array(byteCharacters.length);
  for (let i = 0; i < byteCharacters.length; i++) {
    byteNumbers[i] = byteCharacters.charCodeAt(i);
  }
  const byteArray = new Uint8Array(byteNumbers);
  const blob = new Blob([byteArray], { type: mimeType });
  return new File([blob], filename, { type: mimeType });
};
