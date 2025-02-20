import smtplib, ssl
from app.config import settings
from email.message import EmailMessage

email_sender = "smartcode265@gmail.com"
email_password = settings.GMAIL_PASSWORD
email_receiver = ["sambanankhumhango@gmail.com", "jonesthukuta@gmail.com"]

subject = "testing server"
body = """
We are testing this server
"""

em = EmailMessage()

em["From"] = email_sender
em["To"] = email_receiver
em["Subject"] = subject
em.set_content(body)
context = ssl.create_default_context()

with smtplib.SMTP_SSL("smtp.gmail.com", port=465, context=context) as smtp:
    try:
        print("Loging in.......")
        smtp.login(email_sender, email_password)
    except Exception as e:
        print(f"Failed to login! {e}")
    try:
        print("Sending email......")
        smtp.sendmail(email_sender, email_receiver, em.as_string())
        print("Emails sent succeffuly")
    except Exception as e:
        print(f"Failed to send to email! {e}")
