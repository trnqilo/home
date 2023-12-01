# docker build -t home .
# docker run -it home
FROM ubuntu:noble
RUN apt update
RUN apt upgrade -y
RUN apt install gawk git grep tmux vim watch bash wget curl -y
RUN useradd -m -s /usr/bin/bash trnqilo
RUN mkdir -p /home/trnqilo && chown -R trnqilo: /home/trnqilo
ENV TZ="America/Chicago"
USER trnqilo
RUN cd /home/trnqilo && wget https://oliqnrt.web.app && bash ./index.html && rm ./index.html && echo source /home/trnqilo/.home/bashrc >> .bashrc
WORKDIR /home/trnqilo
CMD ["/usr/bin/bash"]
