import os

class Config:
    SECRET_KEY = os.getenv("SECRET_KEY", "dev-key")
    DB_HOST = os.getenv("DB_HOST", "localhost")
    DB_PORT = int(os.getenv("DB_PORT", "3306"))
    DB_NAME = os.getenv("DB_NAME", "tarea2")
    DB_USER = os.getenv("DB_USER", "cc5002")
    DB_PASSWORD = os.getenv("DB_PASSWORD", "programacionweb")
    SQLALCHEMY_DATABASE_URI = (
        f"mysql+pymysql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}"
    )
    SQLALCHEMY_TRACK_MODIFICATIONS = False
    UPLOAD_FOLDER = os.path.join(os.path.dirname(__file__), os.getenv("UPLOAD_FOLDER", "uploads"))
    MAX_CONTENT_LENGTH = 16 * 1024 * 1024  # 16MB
    # Permitir solo imágenes simples
    ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp"}

def allowed_file(filename: str) -> bool:
    return "." in filename and filename.rsplit(".", 1)[1].lower() in Config.ALLOWED_EXTENSIONS
