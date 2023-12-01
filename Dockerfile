# docker build -t home .
# docker run -it home
FROM ubuntu:noble
RUN apt update
RUN apt upgrade -y
RUN apt install gawk git grep tmux vim watch bash wget curl -y
RUN useradd -m -s /usr/bin/bash trnqilo
RUN echo source ./.home/bashrc > /home/trnqilo/.bashrc
RUN chown -R trnqilo: /home/trnqilo
ENV TZ="America/Chicago"
USER trnqilo
WORKDIR /home/trnqilo
CMD ["/usr/bin/bash"]
