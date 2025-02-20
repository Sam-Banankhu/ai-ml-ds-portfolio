
from app.api.v1.schemas import EmailSendRequest
from app.api.v1.utils import send_otp_to_email,generate_otp

email_data = EmailSendRequest(email="jonesthukuta@gmail.com", otp=generate_otp())
send_otp_to_email(email_data)