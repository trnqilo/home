import 'package:flutter_movie_app/search/events.dart';
import 'package:flutter_movie_app/search/search_bloc.dart';
import 'package:flutter_movie_app/search/states.dart';

class FakeSearchBloc extends SearchBloc {
  List<MovieState> states = [];
  List<MovieEvent> events = [];

  FakeSearchBloc() {
    on<MovieEvent>((event, emit) {
      events = [...events, event];
      for (MovieState state in states) {
        emit(state);
      }
      states = [];
    });
  }
}
