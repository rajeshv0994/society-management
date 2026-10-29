FROM nginx:1.29-alpine

COPY deploy/nginx.conf /etc/nginx/conf.d/default.conf
COPY dist/frontend/ /usr/share/nginx/html/
COPY dist/super-admin/ /usr/share/nginx/html/super-admin/
COPY dist/society-admin/ /usr/share/nginx/html/society-admin/
COPY dist/staff/ /usr/share/nginx/html/staff/
COPY dist/member/ /usr/share/nginx/html/member/

EXPOSE 80
