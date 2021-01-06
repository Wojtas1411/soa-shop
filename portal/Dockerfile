### STAGE 1: Build ###
FROM trion/ng-cli AS builder
COPY ./*.json ./
COPY src src
RUN npm ci
RUN ls src
RUN ng build --prod

### STAGE 2: Run ###
FROM nginx
COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=builder /app/build/app /usr/share/nginx/html
